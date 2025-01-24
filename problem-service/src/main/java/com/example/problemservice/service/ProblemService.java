package com.example.problemservice.service;

import com.example.problemservice.client.BoilerplateClient;
import com.example.problemservice.dto.request.problem.ProblemCreationRequest;
import com.example.problemservice.dto.response.DefaultCode.DefaultCodeResponse;
import com.example.problemservice.dto.response.Problem.ProblemCreationResponse;
import com.example.problemservice.dto.response.Problem.ProblemRowResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.mapper.DefaultCodemapper;
import com.example.problemservice.mapper.ProblemMapper;
import com.example.problemservice.model.*;
import com.example.problemservice.model.composite.DefaultCodeId;
import com.example.problemservice.repository.DefaultCodeRepository;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.repository.ProblemSubmissionRepository;
import com.example.problemservice.repository.ProgrammingLanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;
    private final MarkdownService markdownService;
    private final ProblemSubmissionRepository problemSubmissionRepository;
    private final BoilerplateClient boilerplateClient;
    private final DefaultCodeRepository defaultCodeRepository;
    private final ProgrammingLanguageRepository programmingLanguageRepository;
    private final DefaultCodemapper defaultCodemapper;

    public ProblemCreationResponse createProblem(ProblemCreationRequest problem) {
        Problem savedProblem =  problemRepository.save(problemMapper.toProblem(problem));
        markdownService.saveProblemAsMarkdown(savedProblem);
        return problemMapper.toProblemCreationResponse(savedProblem);
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
        List<ProblemSubmission> submissions = problemSubmissionRepository.findProblemSubmissionByUserUidAndProblem_ProblemId(userId, problemId);
        if (submissions.isEmpty() || submissions == null) {
            return false;
        }
        for(ProblemSubmission submission:submissions)
        {
            List<TestCase_Output> testcaseOutputs = submission.getTestCases_output();
            for (TestCase_Output testcase : testcaseOutputs) {
                if (!testcase.getResult_status().equals("ACCEPTED"))
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
        markdownService.deleteProblemFolder(problem.getProblemName());
        problemRepository.deleteById(problemId);
    }

    public ProblemCreationResponse updateProblem(UUID problemId, ProblemCreationRequest request) {
        Problem existingProblem = problemRepository.findById(problemId).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );

        problemMapper.updateProblemFromRequest(request, existingProblem);
        Problem updatedProblem = problemRepository.save(existingProblem);

        // Update the Markdown files
        markdownService.saveProblemAsMarkdown(updatedProblem);

        return problemMapper.toProblemCreationResponse(updatedProblem);
    }

    public List<DefaultCodeResponse> generateDefaultCodes(UUID problemId, String structure) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(()-> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        // Đọc file từ đường dẫn: D:\sources\Github\intellab_be\problems\two-sum\Structure.md

//        structure = "Problem Name: \"Sum of Two Numbers\"\n" +
//                "Function Name: sum\n" +
//                "Input Structure:\n" +
//                "Input Field: int num1\n" +
//                "Input Field: int num2\n" +
//                "Output Structure:\n" +
//                "Output Field: int result";


        List<ProgrammingLanguage> programmingLanguages = programmingLanguageRepository.findAll();

        for (ProgrammingLanguage programmingLanguage : programmingLanguages) {
            String defaultCode = boilerplateClient.defaultCodeGenerator(structure,programmingLanguage.getId());
            DefaultCodeId id = new DefaultCodeId(programmingLanguage.getId(), problemId);

            DefaultCode new_defaultCode = new DefaultCode();
            new_defaultCode.setDefaultCodeId(id);
            new_defaultCode.setCode(defaultCode);
            new_defaultCode.setProblem(problem);
            new_defaultCode.setLanguage(programmingLanguage);
            defaultCodeRepository.save(new_defaultCode);
        }
        List<DefaultCode> defaultCodes = defaultCodeRepository.findByProblem(problem);
        return defaultCodes.stream().map(defaultCodemapper::toResponse).toList();
    }

    public String enrichCode(String structure, String code, Integer languageId) {
        return boilerplateClient.enrich(code,languageId,structure);
    }

}