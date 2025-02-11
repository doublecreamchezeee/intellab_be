package com.example.problemservice.service;

import com.example.problemservice.dto.request.problem.ProblemCreationRequest;
import com.example.problemservice.dto.response.Problem.ProblemCreationResponse;
import com.example.problemservice.dto.response.Problem.ProblemRowResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.mapper.ProblemMapper;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemSubmission;
import com.example.problemservice.model.TestCaseOutput;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.repository.ProblemSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;
    private final MarkdownService markdownService;
    private final ProblemSubmissionRepository problemSubmissionRepository;

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
        List<ProblemSubmission> submissions = problemSubmissionRepository.findAllByUserIdAndProblem_ProblemId(userId, problemId);
        if (submissions.isEmpty() || submissions == null) {
            return false;
        }
        for(ProblemSubmission submission:submissions)
        {
            List<TestCaseOutput> testcaseOutputs = submission.getTestCasesOutput();
            for (TestCaseOutput testcase : testcaseOutputs) {
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
}