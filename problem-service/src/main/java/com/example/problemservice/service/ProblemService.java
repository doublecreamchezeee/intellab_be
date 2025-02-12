package com.example.problemservice.service;

import com.example.problemservice.converter.ProblemStructureConverter;
import com.example.problemservice.client.BoilerplateClient;
import com.example.problemservice.dto.request.problem.ProblemCreationRequest;
import com.example.problemservice.dto.response.DefaultCode.DefaultCodeResponse;
import com.example.problemservice.dto.response.DefaultCode.PartialBoilerplateResponse;
import com.example.problemservice.dto.response.Problem.ProblemCreationResponse;
import com.example.problemservice.dto.response.Problem.ProblemRowResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.mapper.DefaultCodeMapper;
import com.example.problemservice.mapper.ProblemMapper;
import com.example.problemservice.model.*;
import com.example.problemservice.model.composite.DefaultCodeId;
import com.example.problemservice.repository.DefaultCodeRepository;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemSubmission;
import com.example.problemservice.model.TestCaseOutput;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.utils.MarkdownUtility;
import com.example.problemservice.utils.TestCaseFileReader;
import com.example.problemservice.repository.ProblemSubmissionRepository;
import com.example.problemservice.repository.ProgrammingLanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;
    private final ProblemSubmissionRepository problemSubmissionRepository;
    private final BoilerplateClient boilerplateClient;
    private final DefaultCodeRepository defaultCodeRepository;
    private final ProgrammingLanguageRepository programmingLanguageRepository;
    private final DefaultCodeMapper defaultCodeMapper;

    public ProblemCreationResponse createProblem(ProblemCreationRequest request) {
        Problem problem = problemMapper.toProblem(request);

        problem.setProblemStructure(
                ProblemStructureConverter.convertObjectToString(
                        request.getProblemStructure()
                )
        );

        Problem savedProblem =  problemRepository.save(problem);

        MarkdownUtility.saveProblemAsMarkdown(savedProblem);


        //return response
        ProblemCreationResponse response = problemMapper
                .toProblemCreationResponse(savedProblem);

        response.setProblemStructure(
                request.getProblemStructure()
        );

        generateDefaultCodes(
                savedProblem.getProblemId(),
                savedProblem.getProblemStructure()
        );

        return response;
    }

    public Problem getProblem(UUID problemId) {
        return problemRepository.findById(problemId).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );
    }

    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    public Page<ProblemRowResponse> searchProblems(Pageable pageable, String keyword) {
        Page<Problem> problems = problemRepository.findAllByProblemNameContainingIgnoreCase(keyword, pageable);
        return problems.map(problemMapper::toProblemRowResponse);
    }

    public Page<ProblemRowResponse> searchProblems(Pageable pageable, String keyword, UUID userId) {
        Page<Problem> problems = problemRepository.findAllByProblemNameContainingIgnoreCase(keyword, pageable);

        Page<ProblemRowResponse> results = problems.map(problemMapper::toProblemRowResponse);

        results.forEach(problemRowResponse -> {
            problemRowResponse.setDone(isDoneProblem(problemRowResponse.getProblemId(),userId));
        });
        return results;
    }

    public boolean isDoneProblem(UUID problemId, UUID userId) {
        List<ProblemSubmission> submissions = problemSubmissionRepository.findAllByUserIdAndProblem_ProblemId(userId, problemId);
        if (submissions.isEmpty() || submissions == null) {
            return false;
        }
        for(ProblemSubmission submission:submissions)
        {
            List<TestCaseOutput> testcaseOutputs = submission.getTestCasesOutput();
            for (TestCaseOutput testcase : testcaseOutputs) {
                if (!testcase.getResult_status().equals("Accepted"))
                    return false;
            }
        }
        return true;
    }

    public Page<ProblemRowResponse> getAllProblems(String category, Pageable pageable) {
        Page<Problem> problems = problemRepository.findAll(pageable);
        return problems.map(problemMapper::toProblemRowResponse);
    }

    public void deleteProblem(UUID problemId) {
        Problem problem = problemRepository.findById(problemId).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );
        MarkdownUtility.deleteProblemFolder(problem.getProblemName());
        problemRepository.deleteById(problemId);
    }

    public ProblemCreationResponse updateProblem(UUID problemId, ProblemCreationRequest request) {
        Problem existingProblem = problemRepository.findById(problemId).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );

        String problemStructure = ProblemStructureConverter.convertObjectToString(
                request.getProblemStructure()
        );

        existingProblem.setProblemStructure(problemStructure);

        problemMapper.updateProblemFromRequest(request, existingProblem);
        Problem updatedProblem = problemRepository.save(existingProblem);

        // Update the Markdown files
        MarkdownUtility.saveProblemAsMarkdown(updatedProblem);

        return problemMapper.toProblemCreationResponse(updatedProblem);
    }

    public List<DefaultCodeResponse> generateDefaultCodes(UUID problemId, String structure) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(()-> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        List<ProgrammingLanguage> programmingLanguages = programmingLanguageRepository.findAll();

        for (ProgrammingLanguage programmingLanguage : programmingLanguages) {
            String defaultCode = BoilerplateClient.BoilerPlateGenerator.defaultCodeGenerator(structure, programmingLanguage.getId());
            DefaultCodeId id = new DefaultCodeId(programmingLanguage.getId(), problemId);

            DefaultCode new_defaultCode = new DefaultCode();
            new_defaultCode.setDefaultCodeId(id);
            new_defaultCode.setCode(defaultCode);
            new_defaultCode.setProblem(problem);
            new_defaultCode.setLanguage(programmingLanguage);
            defaultCodeRepository.save(new_defaultCode);
        }
        List<DefaultCode> defaultCodes = defaultCodeRepository.findByProblem(problem);
        return defaultCodes.stream().map(defaultCodeMapper::toResponse).toList();
    }

    public String enrichCode(String structure, String code, Integer languageId) {
        return boilerplateClient.enrich(code,languageId,structure);
    }


    public void getProblemById(UUID problemId) {
        Problem problem =  problemRepository.findById(problemId).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );

        String problemContent = MarkdownUtility.readMarkdownFromFile(
                problem.getProblemName(), "Problem.md");

        List<String> inputs = TestCaseFileReader.getProblemTestCases(
                problem.getProblemName(), TestCaseFileReader.INPUT);

        List<String> outputs = TestCaseFileReader.getProblemTestCases(
                problem.getProblemName(), TestCaseFileReader.OUTPUT);

        log.info("Problem Content: {}", problemContent);
        log.info("Inputs: {}", inputs);
        log.info("Outputs: {}", outputs);

        return;

    }

    public List<PartialBoilerplateResponse> getPartialBoilerplateOfProblem(UUID problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
                );

        List<DefaultCode> listFunctionBoilerplate = defaultCodeRepository.findByProblem(problem);

        return listFunctionBoilerplate.stream().map(defaultCodeMapper::toPartialBoilerplateResponse).toList();
    }

    public void generateBoilerplate() {
        List<Problem> problems = problemRepository.findAll();
        for (Problem problem : problems) {
            String problemStructure = MarkdownUtility.readMarkdownFromFile(
                    problem.getProblemName(), "Structure.md");
            generateDefaultCodes(problem.getProblemId(), problemStructure);
        }
    }
}