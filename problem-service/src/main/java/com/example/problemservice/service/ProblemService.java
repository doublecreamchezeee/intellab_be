package com.example.problemservice.service;

import com.example.problemservice.client.CourseClient;
import com.example.problemservice.converter.ProblemStructureConverter;
import com.example.problemservice.client.BoilerplateClient;
import com.example.problemservice.dto.request.course.CheckingUserCourseExistedRequest;
import com.example.problemservice.dto.request.problem.ProblemCreationRequest;
import com.example.problemservice.dto.response.DefaultCode.DefaultCodeResponse;
import com.example.problemservice.dto.response.DefaultCode.PartialBoilerplateResponse;
import com.example.problemservice.dto.response.Problem.*;
import com.example.problemservice.enums.PremiumPackage;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.mapper.DefaultCodeMapper;
import com.example.problemservice.mapper.ProblemMapper;
import com.example.problemservice.mapper.ProblemcategoryMapper;
import com.example.problemservice.model.*;
import com.example.problemservice.model.ViewSolutionBehavior;
import com.example.problemservice.model.composite.DefaultCodeId;
import com.example.problemservice.model.composite.ProblemCategoryID;
import com.example.problemservice.model.course.Category;
import com.example.problemservice.repository.*;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemSubmission;
import com.example.problemservice.repository.specification.ProblemSpecification;
import com.example.problemservice.utils.MarkdownUtility;
import com.example.problemservice.utils.TestCaseFileReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    private final ProblemcategoryMapper problemcategoryMapper;
    private final CourseClient courseClient;
    private final ProblemCategoryRepository problemCategoryRepository;
    private final ViewSolutionBehaviorRepository viewSolutionBehaviorRepository;

    private <T> Page<T> convertListToPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        List<T> subList = list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }

    public List<ProblemDescriptionResponse> getProblemsDescription(String keyword, List<Integer> categoryIds, String level){
        Specification<Problem> specification = Specification.where(
                ProblemSpecification.categoriesFilter(categoryIds)
                        .and(ProblemSpecification.levelFilter(level))
                        .and(ProblemSpecification.NameFilter(keyword)));

        List<Problem> problems = problemRepository.findAll(specification);
        return problems.stream().map(
                problem -> {
                    ProblemDescriptionResponse problemDescriptionResponse = new ProblemDescriptionResponse();
                    problemDescriptionResponse.setProblemId(problem.getProblemId());
                    problemDescriptionResponse.setProblemName(problem.getProblemName());
                    problemDescriptionResponse.setDescription(problem.getDescription());
                    problemDescriptionResponse.setLevel(problem.getProblemLevel());
                    List<Category> categories = courseClient.categories(
                            problem.getCategories()
                                    .stream()
                                    .map(problemCategory -> problemCategory.getProblemCategoryID().getCategoryId())
                                    .toList()
                    ).getResult();
                    problemDescriptionResponse.setCategories(categories);
                    return problemDescriptionResponse;
                }).toList();
    }

    public ProblemCreationResponse createProblem(ProblemCreationRequest request) {
        // 1. Map DTO to entity
        Problem problem = problemMapper.toProblem(request);

        // 2. Serialize problemStructure to String for DB
        problem.setProblemStructure(
                ProblemStructureConverter.convertObjectToString(request.getProblemStructure())
        );

        // 3. Save initial problem to get UUID
        Problem savedProblem = problemRepository.save(problem);

        // 4. Generate ProblemCategory entities directly from request.getCategories()
        List<ProblemCategory> problemCategories = request.getCategories().stream()
                .map(categoryId -> {
                    return ProblemCategory.builder()
                            .problemCategoryID(
                                    ProblemCategoryID.builder()
                                            .categoryId(categoryId)
                                            .problemId(savedProblem.getProblemId())
                                            .build()
                            )
                            .problem(savedProblem)
                            .build();
                })
                .toList();

        // 5. Save associations
        problemCategoryRepository.saveAll(problemCategories);

        // 6. Optionally set categories in the saved entity (not strictly necessary unless used later)
        savedProblem.setCategories(problemCategories);

        // 7. Save problem markdown file
        MarkdownUtility.saveProblemAsMarkdown(savedProblem);

        // 8. Generate default boilerplate code
        generateDefaultCodes(
                savedProblem.getProblemId(),
                savedProblem.getProblemStructure()
        );

        // 9. Map to response and return
        ProblemCreationResponse response = problemMapper.toProblemCreationResponse(savedProblem);
        response.setProblemStructure(request.getProblemStructure());

        return response;
    }


    public DetailsProblemResponse getProblem(UUID problemId, String subscriptionPlan, UUID userUuid) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));
        DetailsProblemResponse response = problemMapper.toProblemDetailsResponse(problem);

        System.out.println(userUuid + "," + problemId);
        response.setViewedSolution(
                viewSolutionBehaviorRepository.findByProblemIdAndUserId(problemId, userUuid) != null
        );

        List<Category> category = courseClient.categories(
                problem.getCategories()
                        .stream()
                        .map(problemCategory -> problemCategory.getProblemCategoryID().getCategoryId())
                        .toList()
        ).getResult();

        response.setCategories(category);

        response.setIsSolved(isDoneProblem(problemId,userUuid));

        if (response.getIsPublished()
            || subscriptionPlan.equals(PremiumPackage.PREMIUM_PLAN.getCode())
            || subscriptionPlan.equals(PremiumPackage.ALGORITHM_PLAN.getCode())
        ) {
            return response;
        }

        // Check if the problem is in the course plan,
        // so that the user can access this private problem
        if (subscriptionPlan.equals(
                PremiumPackage.COURSE_PLAN.getCode()
        )) {
            Boolean hasUserAlreadyEnrollCourse = courseClient.checkEnrolled(
                    CheckingUserCourseExistedRequest.builder()
                            .problemId(problemId)
                            .userUuid(userUuid)
                            .build()
            ).getResult();

            log.info("hasUserAlreadyEnrollCourse: {}", hasUserAlreadyEnrollCourse);

            if (hasUserAlreadyEnrollCourse) {
                return response;
            }
        }

        throw new AppException(ErrorCode.PROBLEM_NOT_PUBLISHED);
    }

    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    public Page<ProblemRowResponse> searchProblems(List<Integer> categories,
                                                   String level,
                                                   Pageable pageable,
                                                   String keyword) {
        Specification<Problem> specification = Specification.where(
                ProblemSpecification.categoriesFilter(categories)
                        .and(ProblemSpecification.levelFilter(level))
                        .and(ProblemSpecification.NameFilter(keyword)));

        Page<Problem> problems = problemRepository.findAll(specification,pageable);

        return getProblemRowResponses(problems);
    }

    public Page<ProblemRowResponse> searchProblems(List<Integer> categories, String level, Boolean status, Pageable pageable, String keyword, UUID userId) {
        Specification<Problem> specification = Specification.where(
                ProblemSpecification.categoriesFilter(categories)
                        .and(ProblemSpecification.levelFilter(level))
                        .and(ProblemSpecification.NameFilter(keyword))
                        .and(ProblemSpecification.StatusFilter(status,userId)));

        Page<Problem> problems = problemRepository.findAll(specification,pageable);

        Page<ProblemRowResponse> results = getProblemRowResponses(userId, problems);

        results.forEach(problemRowResponse -> {
            problemRowResponse.setIsDone(isDoneProblem(problemRowResponse.getProblemId(),userId));
        });
        return results;
    }

    public boolean isDoneProblem(UUID problemId, UUID userId) {
        List<ProblemSubmission> submissions = problemSubmissionRepository.findAllByUserIdAndProblem_ProblemId(userId, problemId);
        if (submissions == null || submissions.isEmpty()) {
            return false;
        }
        for (ProblemSubmission submission:submissions)
        {
            if (submission.getIsSolved())
                return true;
        }
        return false;
    }

    public Page<ProblemRowResponse> getAllProblems(List<Integer> categories, String level, Boolean status,  Pageable pageable, UUID userId) {
        Specification<Problem> specification = Specification.where(
                ProblemSpecification.categoriesFilter(categories)
                        .and(ProblemSpecification.levelFilter(level))
                        .and(ProblemSpecification.StatusFilter(status,userId)));

        Page<Problem> problems = problemRepository.findAll(specification,pageable);

        return getProblemRowResponses(userId, problems);
    }

    @NotNull
    private Page<ProblemRowResponse> getProblemRowResponses(UUID userId, Page<Problem> problems) {
        return problems.map(problem -> {
            ProblemRowResponse response = problemMapper.toProblemRowResponse(problem);
            List<ProblemCategory> problemCategories = problem.getCategories();

            List<CategoryResponse> categories = problemCategories.stream()
                    .map(p-> courseClient.categories(p.getProblemCategoryID().getCategoryId()).getResult())
                    .toList();

            response.setCategories(categories);

            response.setIsDone(isDoneProblem(response.getProblemId(),userId));
            response.setHasSolution(problem.getSolution() != null);

            return response;
        });
    }

    public Page<ProblemRowResponse> getAllProblems(List<Integer> categories, String level,  Pageable pageable) {

        Specification<Problem> specification = Specification.where(
                ProblemSpecification.categoriesFilter(categories)
                        .and(ProblemSpecification.levelFilter(level)));

        Page<Problem> problems = problemRepository.findAll(specification,pageable);

        return getProblemRowResponses(problems);
    }

    public Page<ProblemRowResponse> getAllProblems(Pageable pageable) {
        Page<Problem> problems = problemRepository.findAll(pageable);

        return getProblemRowResponses(problems);
    }

    @NotNull
    private Page<ProblemRowResponse> getProblemRowResponses(Page<Problem> problems) {
        return problems.map(problem -> {
            ProblemRowResponse response = problemMapper.toProblemRowResponse(problem);
            List<ProblemCategory> problemCategories = problem.getCategories();

            List<CategoryResponse> categories = problemCategories.stream()
                    .map(p-> courseClient.categories(p.getProblemCategoryID().getCategoryId()).getResult())
                    .toList();

            response.setCategories(categories);
            response.setHasSolution( problem.getSolution() != null);

            return response;
        });
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

    @Transactional
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

    @Transactional
    public void generateBoilerplate() {
        defaultCodeRepository.deleteAll();

        List<Problem> problems = problemRepository.findAll();
        for (Problem problem : problems) {
//            String problemStructure = MarkdownUtility.readMarkdownFromFile(
//                    problem.getProblemName(), "Structure.md");
            generateDefaultCodes(problem.getProblemId(), problem.getProblemStructure());
        }
    }
    public Boolean viewSolution(UUID problemId, UUID userId) {
        ViewSolutionBehavior viewSolutionBehavior = new ViewSolutionBehavior();
        viewSolutionBehavior.setProblemId(problemId);
        viewSolutionBehavior.setUserId(userId);
        viewSolutionBehaviorRepository.save(viewSolutionBehavior);
        return true;
    }

    /*@EventListener(ApplicationReadyEvent.class)
    public void generateData() {
        generateBoilerplate();
    }*/
   /* @PostConstruct
    public void init() {
        generateBoilerplate();
    }*/
}