DROP FUNCTION IF EXISTS get_lessons_and_learning_progress(uuid, uuid);

CREATE OR REPLACE FUNCTION get_lessons_and_learning_progress(
    student_id UUID,
    course2_id UUID
)
RETURNS TABLE(
	learning_id UUID,
    lesson_id UUID,
    course_id UUID,
    lesson_order INT,
    lesson_name VARCHAR,
    description TEXT,
    content TEXT,
    problem_id UUID,
    exercise_id UUID,
    status VARCHAR,
    last_accessed_date TIMESTAMP WITH TIME ZONE,
	is_done_theory BOOLEAN,
	is_done_practice BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    ll.learning_id,
    l.lesson_id,
    l.Course_ID,
    l.Lesson_Order,
    l.Lesson_Name,
    l.Description,
    l.Content,
    l.Problem_ID,
    l.Exercise_ID,
    ll.Status,
    ll.Last_accessed_date,
    ll.is_done_theory,
    ll.is_done_practice
FROM
    Lessons l
        JOIN
    Learning_Lesson ll
    ON
        l.lesson_id = ll.lesson_id
WHERE
    ll.user_id = student_id
  AND l.course_id = course2_id
ORDER BY l.lesson_order;
END;
$$;

DROP FUNCTION IF EXISTS caculate_lesson_count(uuid);
CREATE FUNCTION caculate_lesson_count (c_id UUID)
    RETURNS Int AS $$
BEGIN

RETURN COALESCE((select count (*) from lessons where course_id = c_id), 0);
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS caculate_avg_review(uuid);
CREATE FUNCTION caculate_avg_review (c_id UUID)
    RETURNS NUMERIC(5,2) AS $$
BEGIN

RETURN COALESCE((select avg(rating) from reviews where course_id = c_id), 0);
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS caculate_review_count(uuid);
CREATE FUNCTION caculate_review_count (c_id UUID)
    RETURNS Int AS $$
BEGIN

RETURN COALESCE((select count(reviews.review_id) from reviews where course_id = c_id), 0);
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS get_latest_lesson(uuid, uuid);
CREATE OR REPLACE FUNCTION get_latest_lesson(courseId UUID, userId UUID)
RETURNS UUID AS $$
BEGIN
RETURN
    (SELECT uc.latest_lesson_id
     FROM user_courses AS uc
     WHERE uc.user_uid = userId
       AND uc.course_id = courseId
    );
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS check_user_enrolled_course(uuid, uuid);
CREATE OR REPLACE FUNCTION check_user_enrolled_course(courseId UUID, userId UUID)
RETURNS BOOLEAN AS $$
DECLARE
exists BOOLEAN;
BEGIN
SELECT EXISTS (
    SELECT 1
    FROM user_courses
    WHERE user_uid = userId
      AND course_id = courseId
) INTO exists;
RETURN exists;
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS calculate_completion_ratio(uuid, uuid);
CREATE OR REPLACE FUNCTION calculate_completion_ratio(courseId UUID, userId UUID)
RETURNS FLOAT AS $$
DECLARE
total_count INT;
    done_theory_count INT;
	done_practice_count INT;
    completion_ratio FLOAT;
BEGIN
    -- Calculate the total number of theories and practices
SELECT COUNT(*)
INTO total_count
FROM lessons l
         JOIN learning_lesson ll ON l.lesson_id = ll.lesson_id
WHERE l.course_id = courseId
  AND ll.user_id = userId;

-- Calculate the number of completed theories
SELECT COUNT(*)
INTO done_theory_count
FROM lessons l
         JOIN learning_lesson ll ON l.lesson_id = ll.lesson_id
WHERE l.course_id = courseId
  AND ll.user_id = userId
  AND (ll.is_done_theory IS TRUE);

-- Calculate the number of completed theories and practices
SELECT COUNT(*)
INTO done_practice_count
FROM lessons l
         JOIN learning_lesson ll ON l.lesson_id = ll.lesson_id
WHERE l.course_id = courseId
  AND ll.user_id = userId
  AND (ll.is_done_practice IS TRUE);

-- Calculate the completion ratio
IF total_count > 0 THEN
        completion_ratio := (done_theory_count::FLOAT + done_practice_count::FLOAT) * 100 / (total_count::FLOAT * 2);
ELSE
        completion_ratio := 0;
END IF;

RETURN completion_ratio;
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS get_details_course(uuid, uuid);
CREATE OR REPLACE FUNCTION get_details_course(
    courseId UUID,
	userId UUID
)
RETURNS TABLE(
	course_id UUID,
    course_name VARCHAR(255),
    description TEXT,
    level VARCHAR(20),
    price NUMERIC(11,2),
    unit_price VARCHAR(10),
    user_uid UUID,
    lesson_count INT,
    average_rating NUMERIC(5,2),
    review_count INT,
	is_user_enrolled BOOLEAN,
	latest_lesson_id UUID,
	progress_percent FLOAT
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    c.course_id,
    c.course_name,
    c.description,
    c.level,
    c.price,
    c.unit_price,
    c.user_uid,
    caculate_lesson_count(c.course_id) AS lesson_count,
    caculate_avg_review(c.course_id) as average_rating,
    caculate_review_count(c.course_id) as review_count,
    check_user_enrolled_course(c.course_id, userId) as is_user_enrolled,
    get_latest_lesson(c.course_id, userId) as latest_lesson_id,
    calculate_completion_ratio(c.course_id, userId) as progress_percent
FROM
    Courses c
WHERE
    c.course_id = courseId;
END;
$$;

DROP FUNCTION IF EXISTS check_is_done_theory(uuid);
CREATE OR REPLACE FUNCTION check_is_done_theory(learningId UUID)
RETURNS BOOLEAN AS $$
DECLARE
exists BOOLEAN;
BEGIN
SELECT EXISTS (
    SELECT 1
    FROM assignments
    WHERE learning_id = learningId
) INTO exists;
RETURN exists;
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS get_next_lesson_id(uuid, uuid);
CREATE OR REPLACE FUNCTION get_next_lesson_id(courseId UUID, userId UUID)
RETURNS UUID AS $$
BEGIN
RETURN
    (SELECT
         l.lesson_id
     FROM
         learning_lesson ll
             JOIN
         lessons l
         ON ll.lesson_id = l.lesson_id
     WHERE
         ll.user_id = userId
       AND ll.status = 'NEW'
       AND l.course_id = courseId
     ORDER BY
         l.lesson_order
    LIMIT 1);

IF NOT FOUND THEN
        RETURN (SELECT NULL::UUID);
END IF;
END;
$$ LANGUAGE plpgsql;


DROP FUNCTION IF EXISTS get_next_lesson_name(uuid, uuid);
CREATE OR REPLACE FUNCTION get_next_lesson_name(courseId UUID, userId UUID)
RETURNS VARCHAR(255) AS $$
BEGIN
RETURN
    (SELECT
         l.lesson_name
     FROM
         learning_lesson ll
             JOIN
         lessons l
         ON ll.lesson_id = l.lesson_id
     WHERE
         ll.user_id = userId
       AND ll.status = 'NEW'
       AND l.course_id = courseId
     ORDER BY
         l.lesson_order
    LIMIT 1);

IF NOT FOUND THEN
        RETURN (SELECT NULL::VARCHAR(255));
END IF;
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS get_details_lesson(uuid, uuid);
CREATE OR REPLACE FUNCTION get_details_lesson(
    lessonId UUID,
	userId UUID
)
RETURNS TABLE(
	lesson_id UUID,
    content TEXT,
    description TEXT,
	lesson_order INT,
    lesson_name VARCHAR(255),
    course_id UUID,
    exercise_id UUID,
	problem_id UUID,
	learning_id UUID,
    next_lesson_id UUID,
    next_lesson_name VARCHAR(255),
    is_done_theory BOOLEAN,
	is_done_practice BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    l.lesson_id,
    l.content,
    l.description,
    l.lesson_order,
    l.lesson_name,
    l.course_id,
    l.exercise_id,
    l.problem_id,
    lu.learning_id,
    get_next_lesson_id(l.course_id, userId) as next_lesson_id,
    get_next_lesson_name(l.course_id, userId) AS next_lesson_name,
    lu.is_done_theory,
    lu.is_done_practice
--DONT DELETE THIS COMMENTED CODE
--check_is_done_theory(lu.learning_id) as is_done_theory,
--false as is_done_practice
FROM Lessons AS l
         LEFT JOIN
     (SELECT
          ll.learning_id,
          ll.lesson_id,
          ll.is_done_theory,
          ll.is_done_practice
      FROM Learning_lesson AS ll
      WHERE ll.user_id = userId) as lu
     ON	lu.lesson_id = l.lesson_id
WHERE l.lesson_id = lessonId;
END;
$$;

DROP PROCEDURE IF EXISTS mark_theory_of_lesson_as_done(uuid, uuid);

CREATE OR REPLACE PROCEDURE mark_theory_of_lesson_as_done(
    IN learningID UUID,
    IN exerciseId UUID,
    OUT is_done_theory BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
SELECT EXISTS (
    SELECT 1
    FROM
        (SELECT
             ad.answer,
             ad.question_id
         FROM
             learning_lesson ll
                 JOIN
             assignments as a
             ON ll.learning_id = a.learning_id
                 JOIN
             assignment_details as ad
             ON ad.assignment_id = a.assignment_id
         WHERE
             ll.learning_id = learningID) AS ans
            JOIN
        (SELECT
             Q.question_id,
             Q.correct_answer
         FROM exercises AS e
                  JOIN question_list as QL
                       ON e.exercise_id = QL.exercise_id
                  JOIN questions AS Q
                       ON Q.question_id = QL.question_id
         WHERE e.exercise_id = exerciseId) AS res
        ON res.question_id = ans.question_id
            AND res.correct_answer = ans.answer
) INTO is_done_theory;
END;
$$;