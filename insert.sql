create table if not exists public.problems
(
    problem_id      uuid not null
    primary key,
    acceptance_rate numeric(5, 2),
    category        varchar(50),
    description     text,
    problem_level   varchar(20),
    problem_name    varchar(255),
    score           integer
    );

alter table public.problems
    owner to postgres;

create table if not exists public.solutions
(
    author_id  uuid not null,
    content    text,
    user_id    uuid,
    problem_id uuid not null
    constraint fkrm3misp2p4syk4tcnefnqspbl
    references public.problems,
    primary key (author_id, problem_id)
    );

alter table public.solutions
    owner to postgres;

create table if not exists public.problem_submissions
(
    submission_id        uuid not null
    primary key,
    code                 text,
    programming_language varchar(50),
    score_achieved       integer,
    submit_order         integer,
    user_uid             uuid,
    problem_id           uuid
    constraint fkatyso4hx6mtu96ixk88g328er
    references public.problems
    );

alter table public.problem_submissions
    owner to postgres;

create table if not exists public.test_cases
(
    testcase_id uuid not null
    primary key,
    input       text,
    output      text,
    user_id     uuid,
    problem_id  uuid not null
    constraint fkk0300c33ccc0im12utqoxu0m6
    references public.problems
);

alter table public.test_cases
    owner to postgres;

create table if not exists public.test_case_outputs
(
    token             uuid,
    result_status     varchar(30),
    runtime           real,
    submission_output text,
    submission_id     uuid not null
    constraint fkniol765wvj61q9t6lrlhhx091
    references public.problem_submissions,
    testcase_id       uuid not null
    constraint fkawdt7u0ci9lvfrnphpho4p6xd
    references public.test_cases,
    primary key (submission_id, testcase_id)
    );

alter table public.test_case_outputs
    owner to postgres;

create table if not exists public.exercises
(
    exercise_id   uuid not null
    primary key,
    description   text,
    exercise_name varchar(255)
    );

alter table public.exercises
    owner to postgres;

create table if not exists public.medals
(
    medal_id    uuid         not null
    primary key,
    bonus_score integer,
    image       text,
    medal_name  varchar(255) not null,
    type        varchar(20)
    );

alter table public.medals
    owner to postgres;

create table if not exists public.achievements
(
    user_id       uuid not null,
    achieved_date timestamp(6) with time zone,
                                   medal_id      uuid not null
                                   constraint fkn3w49fym60qqsvsectfeeojck
                                   references public.medals,
                                   primary key (medal_id, user_id)
    );

alter table public.achievements
    owner to postgres;

create table if not exists public.leaderboard
(
    user_id   uuid not null
    primary key,
    score     bigint,
    hierarchy varchar(20),
    rank      integer,
    medal_id  uuid
    constraint fkcclgfeq6toq7fanb1cgxy01m7
    references public.medals
    );

alter table public.leaderboard
    owner to postgres;

create table if not exists public.notifications
(
    notification_order bigint not null,
    user_id            uuid   not null,
    content            text,
    notified_date      timestamp(6) with time zone,
                                        title              varchar(255),
    primary key (notification_order, user_id)
    );

alter table public.notifications
    owner to postgres;

create table if not exists public.questions
(
    question_id      uuid not null
    primary key,
    correct_answer   varchar(255),
    created_at       timestamp(6) with time zone,
                                      question_content text,
                                      question_type    char,
                                      status           varchar(10),
    updated_at       timestamp(6) with time zone
                                      );

alter table public.questions
    owner to postgres;

create table if not exists public.options
(
    option_order integer not null,
    content      varchar(255),
    question_id  uuid    not null
    constraint fkjglnbyg0fqsplv75m2oi42ji1
    references public.questions,
    primary key (option_order, question_id)
    );

alter table public.options
    owner to postgres;

create table if not exists public.question_list
(
    exercise_id uuid not null
    constraint fki72qnw59jeo39x6v1uhe0wb51
    references public.exercises,
    question_id uuid not null
    constraint fkabppvo14rfcil3brx1s9begg2
    references public.questions
);

alter table public.question_list
    owner to postgres;

create table if not exists public.report_options
(
    report_option_id uuid not null
    primary key,
    handle_action    varchar(255),
    report_reason    varchar(255),
    type             varchar(20)
    );

alter table public.report_options
    owner to postgres;

create table if not exists public.streak_records
(
    user_id      uuid not null
    primary key,
    last_access  timestamp(6) with time zone,
                                  status       varchar(10),
    streak_score integer,
    medal_id     uuid
    constraint fkejivk072an77tukw69rx6vplu
    references public.medals
    );

alter table public.streak_records
    owner to postgres;

create table if not exists public.topics
(
    topic_id        uuid not null
    primary key,
    content         text,
    number_of_likes integer,
    post_reach      varchar(10),
    title           varchar(255),
    user_uid        varchar(255)
    );

alter table public.topics
    owner to postgres;

create table if not exists public.comments
(
    comment_id        uuid not null
    primary key,
    content           text,
    created           timestamp(6) with time zone,
                                       last_modified     timestamp(6) with time zone,
                                       number_of_likes   bigint,
                                       reply_level       integer,
                                       user_id           uuid,
                                       parent_comment_id uuid
                                       constraint fkst79ninfcw8cbrihoedh118xk
                                       references public.comments,
                                       topic_id          uuid
                                       constraint fkhanwncw4vn5t5syodhdma5sip
                                       references public.topics
                                       );

alter table public.comments
    owner to postgres;

create table if not exists public.comment_reports
(
    owner_id         uuid not null,
    content          text,
    status           varchar(10),
    user_id          uuid,
    destination_id   uuid not null
    constraint fksscjqt1wyjomldmqvx7u6iqhy
    references public.comments,
    report_option_id uuid not null
    constraint fkfkwxdv8wp5h56wq69jnp5ux3g
    references public.report_options,
    primary key (destination_id, owner_id, report_option_id)
    );

alter table public.comment_reports
    owner to postgres;

create table if not exists public.courses
(
    course_id   uuid not null
    primary key,
    course_logo text,
    course_name varchar(255),
    description text,
    level       varchar(20),
    price       numeric(11, 2),
    unit_price  varchar(10),
    user_uid    uuid,
    topic_id    uuid

    constraint uk23uffat5pnitvcg67ugi4kvck
    unique
    constraint fklljvfay1x0yv1gm2xmd6s7j9b
    references public.topics,

    average_rating numeric(5, 2),
    review_count   integer
    );

alter table public.courses
    owner to postgres;

create table if not exists public.lessons
(
    lesson_id    uuid not null
    primary key,
    content      text,
    description  text,
    lesson_name  varchar(255),
    lesson_order integer,
    problem_id   uuid,
    course_id    uuid not null
    constraint fk2uhy91p0gnptep0xxwaal7gnu
    references public.courses,
    exercise_id  uuid
    constraint uker6gswadtti4suc2pq8wbq94a
    unique
    constraint fkkm6c9l61pmyo1j6a8rpivr85m
    references public.exercises
    );

alter table public.lessons
    owner to postgres;

create table if not exists public.learning_lesson
(
    learning_id        uuid not null
    primary key,
    is_done_practice   boolean,
    is_done_theory     boolean,
    last_accessed_date timestamp(6) with time zone,
                                        status             varchar(10),
    user_id            uuid,
    lesson_id          uuid
    constraint fktl0duxtv32rt2myr59sv7d3r3
    references public.lessons
    );

alter table public.learning_lesson
    owner to postgres;

create table if not exists public.assignments
(
    assignment_id uuid not null
    primary key,
    score         numeric(4, 2),
    submit_date   timestamp(6) with time zone,
                                   submit_order  integer,
                                   exercise_id   uuid
                                   constraint fkadkhyietaewgkc3cj0tr8kfon
                                   references public.exercises,
                                   learning_id   uuid not null
                                   constraint fka5frurhonloarunth7ldi7ahb
                                   references public.learning_lesson
                                   );

alter table public.assignments
    owner to postgres;

create table if not exists public.assignment_details
(
    submit_order  integer not null,
    answer        varchar(20),
    unit_score    numeric(4, 2),
    assignment_id uuid    not null
    constraint fkt8xkuef7x94oj86nfxgq85yg9
    references public.assignments,
    question_id   uuid    not null
    constraint fk2ymomgwidfms2ucwbdqrevu9x
    references public.questions,
    primary key (assignment_id, submit_order)
    );

alter table public.assignment_details
    owner to postgres;

create table if not exists public.other_object_reports
(
    owner_id         uuid not null,
    content          text,
    status           varchar(10),
    user_id          uuid,
    destination_id   uuid not null
    constraint fkg6fa7rtsqjtg9imov1j508ors
    references public.topics,
    report_option_id uuid not null
    constraint fk9ajip7upweyynaymq36mt7mo4
    references public.report_options,
    primary key (destination_id, owner_id, report_option_id)
    );

alter table public.other_object_reports
    owner to postgres;

create table if not exists public.reviews
(
    review_id uuid    not null
    primary key,
    comment   text,
    rating    integer not null,
    user_uid  uuid,
    course_id uuid    not null
    constraint fkl9h49973yigjg39ov07a9mog6
    references public.courses
    );

alter table public.reviews
    owner to postgres;

create table if not exists public.user_courses
(
    user_uid           uuid not null,
    last_accessed_date timestamp(6) with time zone,
                                        progress_percent   numeric(5, 2),
    status             varchar(10),
    latest_lesson_id uuid,
    course_id          uuid not null
    constraint fkcve18frw4nbxwrq0qh78dgipc
    references public.courses,
    primary key (course_id, user_uid)
    );

alter table public.user_courses
    owner to postgres;



INSERT INTO public.exercises (exercise_id, description, exercise_name) VALUES ('78ed7e4e-8f14-4994-9d33-42f1a1199bf8', '', '');
INSERT INTO public.exercises (exercise_id, description, exercise_name) VALUES ('30e683f9-2aea-4a16-81d8-78726d470896', '', '');
INSERT INTO public.exercises (exercise_id, description, exercise_name) VALUES ('72099d95-a4ec-4d5c-bf8c-05b8b022aaaa', '', '');
INSERT INTO public.exercises (exercise_id, description, exercise_name) VALUES ('6cff50e2-98ea-4e30-a430-e5462b59d3a1', '', '');
INSERT INTO public.questions (question_id, correct_answer, created_at, question_content, question_type, status, updated_at) VALUES ('48be4626-120d-491f-8014-c9537a9ba516', '2', '2024-12-12 10:00:00.000000 +00:00', 'What is the correct syntax for printing a variable in C++?', 'S', 'Active', null);
INSERT INTO public.questions (question_id, correct_answer, created_at, question_content, question_type, status, updated_at) VALUES ('b50e51da-9c61-4bc5-8cfa-c976bf95ed34', '1', '2024-12-12 10:30:00.000000 +00:00', 'What is the output of the code: `int a = 10; cout << a;`?', 'S', 'Active', null);
INSERT INTO public.questions (question_id, correct_answer, created_at, question_content, question_type, status, updated_at) VALUES ('d851b7af-b356-4990-890b-001726928500', '1', '2024-12-12 11:00:00.000000 +00:00', 'Is C++ an object-oriented programming language?', 'S', 'Active', null);
INSERT INTO public.questions (question_id, correct_answer, created_at, question_content, question_type, status, updated_at) VALUES ('f2a92f4c-507e-4215-8e3a-c7d012ead38f', '2', '2024-12-12 11:30:00.000000 +00:00', 'What is the correct declaration of the main function in C++?', 'S', 'Active', null);
INSERT INTO public.options (option_order, content, question_id) VALUES (3, 'echo(a);', '48be4626-120d-491f-8014-c9537a9ba516');
INSERT INTO public.options (option_order, content, question_id) VALUES (1, 'print(a);', '48be4626-120d-491f-8014-c9537a9ba516');
INSERT INTO public.options (option_order, content, question_id) VALUES (2, 'cout << a;', '48be4626-120d-491f-8014-c9537a9ba516');
INSERT INTO public.options (option_order, content, question_id) VALUES (2, 'undefined', 'b50e51da-9c61-4bc5-8cfa-c976bf95ed34');
INSERT INTO public.options (option_order, content, question_id) VALUES (3, 'Error', 'b50e51da-9c61-4bc5-8cfa-c976bf95ed34');
INSERT INTO public.options (option_order, content, question_id) VALUES (1, '10', 'b50e51da-9c61-4bc5-8cfa-c976bf95ed34');
INSERT INTO public.options (option_order, content, question_id) VALUES (1, 'Yes', 'd851b7af-b356-4990-890b-001726928500');
INSERT INTO public.options (option_order, content, question_id) VALUES (2, 'No', 'd851b7af-b356-4990-890b-001726928500');
INSERT INTO public.options (option_order, content, question_id) VALUES (3, 'void main(){}', 'f2a92f4c-507e-4215-8e3a-c7d012ead38f');
INSERT INTO public.options (option_order, content, question_id) VALUES (2, 'int main(){}', 'f2a92f4c-507e-4215-8e3a-c7d012ead38f');
INSERT INTO public.options (option_order, content, question_id) VALUES (1, 'main(){}', 'f2a92f4c-507e-4215-8e3a-c7d012ead38f');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('78ed7e4e-8f14-4994-9d33-42f1a1199bf8', '48be4626-120d-491f-8014-c9537a9ba516');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('78ed7e4e-8f14-4994-9d33-42f1a1199bf8', 'b50e51da-9c61-4bc5-8cfa-c976bf95ed34');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('78ed7e4e-8f14-4994-9d33-42f1a1199bf8', 'd851b7af-b356-4990-890b-001726928500');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('78ed7e4e-8f14-4994-9d33-42f1a1199bf8', 'f2a92f4c-507e-4215-8e3a-c7d012ead38f');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('30e683f9-2aea-4a16-81d8-78726d470896', '48be4626-120d-491f-8014-c9537a9ba516');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('30e683f9-2aea-4a16-81d8-78726d470896', 'b50e51da-9c61-4bc5-8cfa-c976bf95ed34');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('30e683f9-2aea-4a16-81d8-78726d470896', 'd851b7af-b356-4990-890b-001726928500');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('30e683f9-2aea-4a16-81d8-78726d470896', 'f2a92f4c-507e-4215-8e3a-c7d012ead38f');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('72099d95-a4ec-4d5c-bf8c-05b8b022aaaa', '48be4626-120d-491f-8014-c9537a9ba516');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('72099d95-a4ec-4d5c-bf8c-05b8b022aaaa', 'b50e51da-9c61-4bc5-8cfa-c976bf95ed34');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('72099d95-a4ec-4d5c-bf8c-05b8b022aaaa', 'd851b7af-b356-4990-890b-001726928500');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('72099d95-a4ec-4d5c-bf8c-05b8b022aaaa', 'f2a92f4c-507e-4215-8e3a-c7d012ead38f');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('6cff50e2-98ea-4e30-a430-e5462b59d3a1', '48be4626-120d-491f-8014-c9537a9ba516');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('6cff50e2-98ea-4e30-a430-e5462b59d3a1', 'b50e51da-9c61-4bc5-8cfa-c976bf95ed34');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('6cff50e2-98ea-4e30-a430-e5462b59d3a1', 'd851b7af-b356-4990-890b-001726928500');
INSERT INTO public.question_list (exercise_id, question_id) VALUES ('6cff50e2-98ea-4e30-a430-e5462b59d3a1', 'f2a92f4c-507e-4215-8e3a-c7d012ead38f');
INSERT INTO public.topics (topic_id, content, number_of_likes, post_reach, title, user_uid) VALUES ('b80a10f9-2846-40f8-bc15-4e21d224e95a', 'What is Stack Data Structure? A Complete Tutorial', null, null, 'Stack', null);
INSERT INTO public.topics (topic_id, content, number_of_likes, post_reach, title, user_uid) VALUES ('4cae2ca4-7a7d-463f-8860-fa0eb431065f', 'Introduction to Queue Data Structure', null, null, 'Queue', null);
INSERT INTO public.topics (topic_id, content, number_of_likes, post_reach, title, user_uid) VALUES ('9211ef62-7bce-48a1-8cf3-5bc8fe3091cb', e'A ****singly linked list**** is a fundamental data structure, it consists of ****nodes**** where each node contains a ****data**** field and a ****reference**** to the next node in the linked list. The next of the last node is ****null****, indicating the end of the list. Linked Lists support efficient
insertion and deletion operations.', null, null, 'Linked List', null);
INSERT INTO public.topics (topic_id, content, number_of_likes, post_reach, title, user_uid) VALUES ('d200871a-c5fd-4f4b-bb5c-f7960191bd78', e'****Array in C**** is one of the most used data structures in C programming. It is a
simple and fast way of storing multiple values under a single name. In
this article, we will study the different aspects of array in C language
such as array declaration, definition, initialization, types of arrays,
array syntax, advantages and disadvantages, and many more.
', null, null, 'Array', null);
INSERT INTO public.courses (course_id, course_logo, course_name, description, level, price, unit_price, user_uid, topic_id) VALUES ('7f481263-762a-4311-94d3-fb3aeed36154', null, 'Stack', 'The Stack lesson series offers a comprehensive introduction to one of the fundamental data structures in programming. You''ll explore how a Stack operates based on the LIFO (Last In, First Out) principle, perform core operations such as push, pop, and peek, and apply these concepts to real-world problems like validating parentheses, converting expressions, or building a browser''s backtracking system. This series is ideal for beginners and those looking to strengthen their understanding of data structures.', 'Beginner', 0.00, 'VND', null, 'b80a10f9-2846-40f8-bc15-4e21d224e95a');
INSERT INTO public.courses (course_id, course_logo, course_name, description, level, price, unit_price, user_uid, topic_id) VALUES ('940fddca-cb93-47d1-a28f-355e3c4490d2', null, 'Queue', 'The Queue lesson series dives into the mechanics of this fundamental data structure, which follows the FIFO (First In, First Out) principle. You’ll learn how to perform operations such as enqueue, dequeue, and peek, while also exploring its variations like Circular Queues and Priority Queues. Practical applications, including task scheduling, buffering, and breadth-first search algorithms, will help solidify your understanding. This series is perfect for beginners and anyone looking to master essential programming concepts.', 'Beginner', 0.00, 'VND', null, '4cae2ca4-7a7d-463f-8860-fa0eb431065f');
INSERT INTO public.courses (course_id, course_logo, course_name, description, level, price, unit_price, user_uid, topic_id) VALUES ('95a8a2cd-95c1-4ad8-9c8c-61b8dfe820c5', null, 'Linked List', 'The Linked List lesson series explores this versatile data structure, where elements (nodes) are dynamically linked using pointers. You’ll understand the differences between Singly, Doubly, and Circular Linked Lists, and learn how to perform operations like insertion, deletion, traversal, and searching. These lessons also highlight the advantages of Linked Lists over arrays, such as dynamic memory allocation. By the end, you''ll be ready to tackle real-world scenarios like memory management and graph representation.', 'Intermediate', 0.00, 'VND', null, '9211ef62-7bce-48a1-8cf3-5bc8fe3091cb');
INSERT INTO public.courses (course_id, course_logo, course_name, description, level, price, unit_price, user_uid, topic_id) VALUES ('b15d3666-a77b-4098-89f1-225327c74f67', null, 'Array', 'The Array lesson series introduces you to this foundational data structure, where elements are stored in a contiguous block of memory. You’ll learn about fixed and dynamic arrays, how to access elements efficiently, and perform operations like sorting, searching, and resizing. With real-world applications such as managing datasets, implementing algorithms, and representing matrices, this series is essential for anyone building a strong foundation in programming.', 'Intermediate', 0.00, 'VND', null, 'd200871a-c5fd-4f4b-bb5c-f7960191bd78');
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('5c92a239-33f5-410a-ac45-81d42968b898', e'![What-is-Stack-(1)](https://media.geeksforgeeks.org/wp-content/uploads/20240606180325/What-is-Stack-(1).webp)
### ****LIFO(Last In First Out) Principle****

Here are some real world examples of LIFO

* Consider a stack of plates. When we add a plate, we add at the top.
  When we remove, we remove from the top.
* A ****shuttlecock box****
  (or any other box that is closed from one end) is another great
  real-world example of the ****LIFO (Last In, First Out)****
  principle where do insertions and removals from the same end.

Representation of Stack Data Structure:
---------------------------------------

Stack follows LIFO (Last In First Out) Principle so the element which
is pushed last is popped first.


![Stack-representation-in-Data-Structures-(1)](https://media.geeksforgeeks.org/wp-content/uploads/20240606180735/Stack-representation-in-Data-Structures-(1).webp)

****Types of Stack:****
-----------------------

* ****Fixed Size Stack****
  : As the name suggests, a fixed size stack has a fixed size and cannot
  grow or shrink dynamically. If the stack is full and an attempt is
  made to add an element to it, an overflow error occurs. If the stack
  is empty and an attempt is made to remove an element from it, an
  underflow error occurs.
* ****Dynamic Size Stack****
  : A dynamic size stack can grow or shrink dynamically. When the stack
  is full, it automatically increases its size to accommodate the new
  element, and when the stack is empty, it decreases its size. This type
  of stack is implemented using a linked list, as it allows for easy
  resizing of the stack.

Basic Operations on Stack:
--------------------------

In order to make manipulations in a stack, there are certain operations
provided to us.


* ****push()****  to insert an element into the stack
* ****pop()****  to remove an element from the stack
* ****top()****  Returns the top element of the stack.
* ****isEmpty()****  returns true if stack is empty else false.
* ****isFull()****  returns true if the stack is full else false.

To implement stack, we need to maintain reference to the top
item.

### ****Push Operation on Stack****

Adds an item to the stack. If the stack is full, then it is said to be
an  ****Overflow condition.****

 ****Algorithm for Push Operation:****

* Before pushing the element to the stack, we check if the stack is  ****full****  .
* If the stack is full  ****(top == capacity-1)****  , then  ****Stack Overflows****  and we cannot insert the element to the stack.
* Otherwise, we increment the value of top by 1  ****(top = top + 1)****  and the new value is inserted at  ****top position****  .
* The elements can be pushed into the stack till we reach the  ****capacity****  of the stack.

![Push-Operation-in-Stack-(1)](https://media.geeksforgeeks.org/wp-content/uploads/20240606180844/Push-Operation-in-Stack-(1).webp)
### ****Pop Operation in Stack****

Removes an item from the stack. The items are popped in the reversed
order in which they are pushed. If the stack is empty, then it is said
to be an  ****Underflow condition.****

****Algorithm for Pop Operation:****

* Before popping the element from the stack, we check if the stack is  ****empty****  .
* If the stack is empty (top == -1), then  ****Stack Underflows****  and we cannot remove any element from the stack.
* Otherwise, we store the value at top, decrement the value of top by 1  ****(top = top – 1)****  and return the stored top value.

![Pop-Operation-in-Stack-(1)](https://media.geeksforgeeks.org/wp-content/uploads/20240606180943/Pop-Operation-in-Stack-(1).webp)
### ****Top or Peek Operation on Stack****

Returns the top element of the stack.

****Algorithm for Top Operation:****

* Before returning the top element from the stack, we check if the
  stack is empty.
* If the stack is empty (top == -1), we simply print “Stack is empty”.
* Otherwise, we return the element stored at  ****index = top****  .

![Top-or-Peek-Operation-in-Stack-(1)](https://media.geeksforgeeks.org/wp-content/uploads/20240606181023/Top-or-Peek-Operation-in-Stack-(1).webp)
### ****isEmpty Operation in Stack Data Structure:****

Returns true if the stack is empty, else false.

****Algorithm for isEmpty Operation****:

* Check for the value of  ****top****  in stack.
* If  ****(top == -1)****, then the stack is  ****empty****  so return  ****true****  .
* Otherwise, the stack is not empty so return  ****false****  .

![isEmpty-Operation-in-Stack-(1)](https://media.geeksforgeeks.org/wp-content/uploads/20240606181101/isEmpty-Operation-in-Stack-(1).webp)
### isFull ****Operation in Stack**** ****Data Structure****:

Returns true if the stack is full, else false.

****Algorithm for isFull Operation:****

* Check for the value of  ****top****  in stack.
* If  ****(top == capacity-1),****  then the stack is  ****full****  so return  ****true****.
* Otherwise, the stack is not full so return  ****false****.

![isFull-Operation-in-Stack-(1)](https://media.geeksforgeeks.org/wp-content/uploads/20240606181147/isFull-Operation-in-Stack-(1).webp)

Implementation of Stack
-----------------------


The basic operations that can be performed on a stack include push, pop,
and peek. There are two ways to implement a stack –


* [****Implementation of Stack using Array****](https://www.geeksforgeeks.org/implement-stack-using-array/)
* [****Implementation of Stack using Linked List****](https://www.geeksforgeeks.org/implement-a-stack-using-singly-linked-list/)

****Complexity Analysis of Operations on Stack Data Structure:****
------------------------------------------------------------------

| ****Operations**** | ****Time Complexity**** | ****Space Complexity**** |
| --- | --- | --- |
| ****push()**** | O(1) | O(1) |
| ****pop()**** | O(1) | O(1) |
| top() or  ****pee****k() | O(1) | O(1) |
| isEmpty() | O(1) | O(1) |
| isFull() | O(1) | O(1) |

****Next Articles:****

* [Applications, Advantages and Disadvantages of Stack](https://www.geeksforgeeks.org/applications-advantages-and-disadvantages-of-stack/)
* [Implement a stack using singly linked list](https://www.geeksforgeeks.org/implement-a-stack-using-singly-linked-list)
* [Basic Operations in Stack Data Structure with Implementations](https://www.geeksforgeeks.org/basic-operations-in-stack-data-structure-with-implementations)
* [Top 50 Problems on Stack Data Structure asked in SDE Interviews](https://www.geeksforgeeks.org/top-50-problems-on-stack-data-structure-asked-in-interviews)
* [Applications, Advantages and Disadvantages of Stack](https://www.geeksforgeeks.org/applications-advantages-and-disadvantages-of-stack)
* [Stack for Competitive Programming](https://www.geeksforgeeks.org/stack-for-competitive-programming)

[Try it on GfG Practice
![redirect icon](https://media.geeksforgeeks.org/auth-dashboard-uploads/Group-arrow.svg)](https://www.geeksforgeeks.org/problems/implement-stack-using-array/1?itm_source=geeksforgeeks&itm_medium=article&itm_campaign=practice_card)

Join
[GfG 160](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium), a 160-day journey of coding challenges aimed at sharpening your
skills. Each day, solve a handpicked problem, dive into detailed
solutions through articles and videos, and enhance your preparation for
any interview—all for free! Plus, win exciting GfG goodies along the
way! -
[Explore Now](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium)

[Next Article](https://www.geeksforgeeks.org/applications-advantages-and-disadvantages-of-stack/?ref=next_article)

[Applications, Advantages and Disadvantages of Stack](https://www.geeksforgeeks.org/applications-advantages-and-disadvantages-of-stack/?ref=next_article)

[![author](https://media.geeksforgeeks.org/auth/profile/sb7ciorr5k5t22woqkes)](https://www.geeksforgeeks.org/user/kartik/contributions/?itm_source=geeksforgeeks&itm_medium=article_author&itm_campaign=auth_user)

[kartik](https://www.geeksforgeeks.org/user/kartik/contributions/?itm_source=geeksforgeeks&itm_medium=article_author&itm_campaign=auth_user)

[![News](https://media.geeksforgeeks.org/auth-dashboard-uploads/Google-news.svg)](https://news.google.com/publications/CAAqBwgKMLTrzwsw44bnAw?hl=en-IN&gl=IN&ceid=IN%3Aen)
', 'Stack is a linear data structure that follows LIFO Last In First Out Principle the last element inserted is the first to be popped out It meansboth insertion and deletion operations happen at one end only', 'Introduction', 1, '7f481263-762a-4311-94d3-fb3aeed36154', '30e683f9-2aea-4a16-81d8-78726d470896');
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('be8d9fb6-12e8-4722-9c2e-0304c25acb15', e'Applications of Stacks:
-----------------------

* ****Function calls:****
  Stacks are used to keep track of the return addresses of function
  calls, allowing the program to return to the correct location after a
  function has finished executing.
* ****Recursion:**** Stacks are used to store the local variables and return addresses of
  recursive function calls, allowing the program to keep track of the
  current state of the recursion.
* ****Expression evaluation:**** Stacks are used to evaluate expressions in postfix notation (Reverse
  Polish Notation).
* ****Syntax parsing:**** Stacks are used to check the validity of syntax in programming
  languages and other formal languages.
* ****Memory management:**** Stacks are used to allocate and manage memory in some operating
  systems and programming languages.
* Used to solve popular problems like [Next Greater](https://www.geeksforgeeks.org/next-greater-element/), [Previous Greater](https://www.geeksforgeeks.org/previous-greater-element/), [Next Smaller](https://www.geeksforgeeks.org/next-smaller-element/), [Previous Smaller](https://www.geeksforgeeks.org/find-the-nearest-smaller-numbers-on-left-side-in-an-array/), [Largest Area in a Histogram](https://www.geeksforgeeks.org/largest-rectangular-area-in-a-histogram-using-stack/) and [Stock Span Problems](https://www.geeksforgeeks.org/the-stock-span-problem/).

Advantages of Stacks:
---------------------

* ****Simplicity:**** Stacks are a simple and easy-to-understand data structure, making
  them suitable for a wide range of applications.
* ****Efficiency:**** Push and pop operations on a stack can be performed in constant time ****(O(1))****, providing efficient access to data.
* ****Last-in, First-out (LIFO):****
  Stacks follow the LIFO principle, ensuring that the last element added
  to the stack is the first one removed. This behavior is useful in many
  scenarios, such as function calls and expression evaluation.
* ****Limited memory usage:**** Stacks only need to store the elements that have been pushed onto
  them, making them memory-efficient compared to other data
  structures.

Disadvantages of Stacks:
------------------------

* ****Limited access:****
  Elements in a stack can only be accessed from the top, making it
  difficult to retrieve or modify elements in the middle of the
  stack.
* ****Potential for overflow:**** If more elements are pushed onto a stack than it can hold, an
  overflow error will occur, resulting in a loss of data.
* ****Not suitable for random access:**** Stacks do not allow for random access to elements, making them
  unsuitable for applications where elements need to be accessed in a
  specific order.
* ****Limited capacity:****
  Stacks have a fixed capacity, which can be a limitation if the number
  of elements that need to be stored is unknown or highly
  variable.', 'A stack is a linear data structurein which the insertion of a new element and removal of an existingelement takes place at the same end represented as the top of the stack', 'Applications', 4, '7f481263-762a-4311-94d3-fb3aeed36154', null);
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('422ba686-e5bf-44ba-977d-a20bc5293fa6', e'Implement Stack using Array:
----------------------------

> To implement a stack using an array, initialize an array and treat
> its end as the stack’s top. Implement ****push**** (add to end), ****pop**** (remove from end), and ****peek**** (check end) operations, handling cases for an ****empty**** or f****ull stack****.

****Step-by-step approach:****

1. ****Initialize an array**** to represent the stack.
2. Use the ****end of the array**** to represent the ****top of the stack****.
3. Implement ****push**** (add to end), ****pop**** (remove from the end), and ****peek**** (check end) operations, ensuring to handle empty and full stack
   conditions.

Implement Stack Operations using Array:
---------------------------------------


Here are the following operations of implement stack using array:

### ****Push Operation in Stack:****

Adds an item to the stack. If the stack is full, then it is said to be
an ****Overflow condition.****

****Algorithm for Push Operation:****

> * Before pushing the element to the stack, we check if the stack
>   is ****full****.
> * If the stack is full ****(top == capacity-1)****, then ****Stack Overflows****and we cannot insert the element to the stack.
> * Otherwise, we increment the value of top by 1 ****(top = top + 1)****and the new value is inserted at ****top position****.
> * The elements can be pushed into the stack till we reach
>   the ****capacity****of the stack.

![push-operation-in-stack-1.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415114219/push-operation-in-stack-1.webp)![push-operation-in-stack-1.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415114219/push-operation-in-stack-1.webp)


![push-operation-in-stack-2.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415114220/push-operation-in-stack-2.webp)![push-operation-in-stack-2.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415114220/push-operation-in-stack-2.webp)


![push-operation-in-stack-3.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415114221/push-operation-in-stack-3.webp)![push-operation-in-stack-3.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415114221/push-operation-in-stack-3.webp)


![push-operation-in-stack-4.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415114222/push-operation-in-stack-4.webp)![push-operation-in-stack-4.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415114222/push-operation-in-stack-4.webp)


![push-operation-in-stack-5.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415114222/push-operation-in-stack-5.webp)![push-operation-in-stack-5.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415114222/push-operation-in-stack-5.webp)


![push-operation-in-stack-6.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415114223/push-operation-in-stack-6.webp)![push-operation-in-stack-6.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415114223/push-operation-in-stack-6.webp)


### ****Pop Operation in Stack:****

Removes an item from the stack. The items are popped in the reversed
order in which they are pushed. If the stack is empty, then it is said
to be an ****Underflow condition.****

****Algorithm for Pop Operation:****

> * Before popping the element from the stack, we check if the stack
>   is ****empty****.
> * If the stack is empty (top == -1), then ****Stack Underflows****and we cannot remove any element from the stack.
> * Otherwise, we store the value at top, decrement the value of top by
>   1 ****(top = top – 1)****and return the stored top value.

![pop-operation-in-stack-1.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415123025/pop-operation-in-stack-1.webp)![pop-operation-in-stack-1.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415123025/pop-operation-in-stack-1.webp)


![pop-operation-in-stack-2.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415123025/pop-operation-in-stack-1.webp)![pop-operation-in-stack-2.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415123025/pop-operation-in-stack-1.webp)


![pop-operation-in-stack-3.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415123025/pop-operation-in-stack-1.webp)![pop-operation-in-stack-3.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415123025/pop-operation-in-stack-1.webp)


![pop-operation-in-stack-4.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415123025/pop-operation-in-stack-1.webp)![pop-operation-in-stack-4.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415123025/pop-operation-in-stack-1.webp)


![pop-operation-in-stack-5.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415123025/pop-operation-in-stack-1.webp)![pop-operation-in-stack-5.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415123025/pop-operation-in-stack-1.webp)


![pop-operation-in-stack-6.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415123025/pop-operation-in-stack-1.webp)![pop-operation-in-stack-6.webp](https://media.geeksforgeeks.org/wp-content/uploads/20240415123025/pop-operation-in-stack-1.webp)

### ****Top or Peek Operation in Stack:****

Returns the top element of the stack.

****Algorithm for Top Operation:****

> * Before returning the top element from the stack, we check if the
>   stack is empty.
> * If the stack is empty (top == -1), we simply print “Stack is
>   empty”.
> * Otherwise, we return the element stored at ****index = top****.

### ****isEmpty Operation in Stack:****

Returns true if the stack is empty, else false.

****Algorithm for isEmpty Operation****:

> * Check for the value of ****top****in stack.
> * If ****(top == -1)****, then the stack is ****empty****so return ****true****.
> * Otherwise, the stack is not empty so return ****false****.

### isFull ****Operation in Stack****:

Returns true if the stack is full, else false.

****Algorithm for isFull Operation:****

> * Check for the value of ****top****in stack.
> * If ****(top == capacity-1),****then the stack is ****full****so return ****true****.
> * Otherwise, the stack is not full so return ****false.****

Below is the implementation of the above approach:

C++
````
/* C++ program to implement basic stack
operations */
#include <bits/stdc++.h>

using namespace std;

#define MAX 1000

class Stack {
    int top;

public:
    int a[MAX]; // Maximum size of Stack

    Stack() { top = -1; }
    bool push(int x);
    int pop();
    int peek();
    bool isEmpty();
};

bool Stack::push(int x)
{
    if (top >= (MAX - 1)) {
        cout << "Stack Overflow";
        return false;
    }
    else {
        a[++top] = x;
        cout << x << " pushed into stack\\n";
        return true;
    }
}

int Stack::pop()
{
    if (top < 0) {
        cout << "Stack Underflow";
        return 0;
    }
    else {
        int x = a[top--];
        return x;
    }
}
int Stack::peek()
{
    if (top < 0) {
        cout << "Stack is Empty";
        return 0;
    }
    else {
        int x = a[top];
        return x;
    }
}

bool Stack::isEmpty()
{
    return (top < 0);
}

// Driver program to test above functions
int main()
{
    class Stack s;
    s.push(10);
    s.push(20);
    s.push(30);
    cout << s.pop() << " Popped from stack\\n";

    //print top element of stack after popping
    cout << "Top element is : " << s.peek() << endl;

    //print all elements in stack :
    cout <<"Elements present in stack : ";
    while(!s.isEmpty())
    {
        // print top element in stack
        cout << s.peek() <<" ";
        // remove top element in stack
        s.pop();
    }

    return 0;
}

````

C
````
// C program for array implementation of stack
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>

// A structure to represent a stack
struct Stack {
    int top;
    unsigned capacity;
    int* array;
};

// function to create a stack of given capacity. It initializes size of
// stack as 0
struct Stack* createStack(unsigned capacity)
{
    struct Stack* stack = (struct Stack*)malloc(sizeof(struct Stack));
    stack->capacity = capacity;
    stack->top = -1;
    stack->array = (int*)malloc(stack->capacity * sizeof(int));
    return stack;
}

// Stack is full when top is equal to the last index
int isFull(struct Stack* stack)
{
    return stack->top == stack->capacity - 1;
}

// Stack is empty when top is equal to -1
int isEmpty(struct Stack* stack)
{
    return stack->top == -1;
}

// Function to add an item to stack. It increases top by 1
void push(struct Stack* stack, int item)
{
    if (isFull(stack))
        return;
    stack->array[++stack->top] = item;
    printf("%d pushed to stack\\n", item);
}

// Function to remove an item from stack. It decreases top by 1
int pop(struct Stack* stack)
{
    if (isEmpty(stack))
        return INT_MIN;
    return stack->array[stack->top--];
}

// Function to return the top from stack without removing it
int peek(struct Stack* stack)
{
    if (isEmpty(stack))
        return INT_MIN;
    return stack->array[stack->top];
}

// Driver program to test above functions
int main()
{
    struct Stack* stack = createStack(100);

    push(stack, 10);
    push(stack, 20);
    push(stack, 30);

    printf("%d popped from stack\\n", pop(stack));

    return 0;
}

````

Java
````
/* Java program to implement basic stack
operations */
class Stack {
    static final int MAX = 1000;
    int top;
    int a[] = new int[MAX]; // Maximum size of Stack

    boolean isEmpty()
    {
        return (top < 0);
    }
    Stack()
    {
        top = -1;
    }

    boolean push(int x)
    {
        if (top >= (MAX - 1)) {
            System.out.println("Stack Overflow");
            return false;
        }
        else {
            a[++top] = x;
            System.out.println(x + " pushed into stack");
            return true;
        }
    }

    int pop()
    {
        if (top < 0) {
            System.out.println("Stack Underflow");
            return 0;
        }
        else {
            int x = a[top--];
            return x;
        }
    }

    int peek()
    {
        if (top < 0) {
            System.out.println("Stack Underflow");
            return 0;
        }
        else {
            int x = a[top];
            return x;
        }
    }

    void print(){
    for(int i = top;i>-1;i--){
    System.out.print(" "+ a[i]);
    }
}
}

// Driver code
class Main {
    public static void main(String args[])
    {
        Stack s = new Stack();
        s.push(10);
        s.push(20);
        s.push(30);
        System.out.println(s.pop() + " Popped from stack");
        System.out.println("Top element is :" + s.peek());
        System.out.print("Elements present in stack :");
        s.print();
    }
}

````

Python3
````
# Python program for implementation of stack

# import maxsize from sys module
# Used to return -infinite when stack is empty
from sys import maxsize

# Function to create a stack. It initializes size of stack as 0
def createStack():
    stack = []
    return stack

# Stack is empty when stack size is 0
def isEmpty(stack):
    return len(stack) == 0

# Function to add an item to stack. It increases size by 1
def push(stack, item):
    stack.append(item)
    print(item + " pushed to stack ")

# Function to remove an item from stack. It decreases size by 1
def pop(stack):
    if (isEmpty(stack)):
        return str(-maxsize -1) # return minus infinite

    return stack.pop()

# Function to return the top from stack without removing it
def peek(stack):
    if (isEmpty(stack)):
        return str(-maxsize -1) # return minus infinite
    return stack[len(stack) - 1]

# Driver program to test above functions
stack = createStack()
push(stack, str(10))
push(stack, str(20))
push(stack, str(30))
print(pop(stack) + " popped from stack")

````

C#
````
// C# program to implement basic stack
// operations
using System;

namespace ImplementStack {
class Stack {
    private int[] ele;
    private int top;
    private int max;
    public Stack(int size)
    {
        ele = new int[size]; // Maximum size of Stack
        top = -1;
        max = size;
    }

    public void push(int item)
    {
        if (top == max - 1) {
            Console.WriteLine("Stack Overflow");
            return;
        }
        else {
            ele[++top] = item;
        }
    }

    public int pop()
    {
        if (top == -1) {
            Console.WriteLine("Stack is Empty");
            return -1;
        }
        else {
            Console.WriteLine("{0} popped from stack ", ele[top]);
            return ele[top--];
        }
    }

    public int peek()
    {
        if (top == -1) {
            Console.WriteLine("Stack is Empty");
            return -1;
        }
        else {
            Console.WriteLine("{0} popped from stack ", ele[top]);
            return ele[top];
        }
    }

    public void printStack()
    {
        if (top == -1) {
            Console.WriteLine("Stack is Empty");
            return;
        }
        else {
            for (int i = 0; i <= top; i++) {
                Console.WriteLine("{0} pushed into stack", ele[i]);
            }
        }
    }
}

// Driver program to test above functions
class Program {
    static void Main()
    {
        Stack p = new Stack(5);

        p.push(10);
        p.push(20);
        p.push(30);
        p.printStack();
        p.pop();
    }
}
}

````

JavaScript
````
/* javascript program to implement basic stack
operations
*/
var t = -1;
    var MAX = 1000;
    var a = Array(MAX).fill(0); // Maximum size of Stack

    function isEmpty() {
        return (t < 0);
    }

    function push(x) {
        if (t >= (MAX - 1)) {
            console.log("Stack Overflow");
            return false;
        } else {
        t+=1;
            a[t] = x;

            console.log(x + " pushed into stack<br/>");
            return true;
        }
    }

    function pop() {
        if (t < 0) {
            console.log("Stack Underflow");
            return 0;
        } else {
            var x = a[t];
            t-=1;
            return x;
        }
    }

    function peek() {
        if (t < 0) {
            console.log("Stack Underflow");
            return 0;
        } else {
            var x = a[t];
            return x;
        }
    }

    function print() {
        for (i = t; i > -1; i--) {
            console.log(" " + a[i]);
        }
    }

        push(10);
        push(20);
        push(30);
        console.log(pop() + " Popped from stack");
        console.log("<br/>Top element is :" + peek());
        console.log("<br/>Elements present in stack : ");
        print();

````

**Output**
```

10 pushed into stack
20 pushed into stack
30 pushed into stack
30 Popped from stack
Top element is : 20
Elements present in stack : 20 10
```
### Complexity Analysis:

* ****Time Complexity****:
  + `push`: O(1)
  + `pop`: O(1)
  + `peek`: O(1)
  + `is_empty`: O(1)
  + is\\_full: O(1)
* ****Auxiliary Space****: O(n), where n is the number of items in the stack.

Advantages of Array Implementation:
-----------------------------------

* Easy to implement.
* Memory is saved as pointers are not involved.

Disadvantages of Array Implementation:
--------------------------------------

* It is not dynamic i.e., it doesn’t grow and shrink depending on needs
  at runtime. [But in case of dynamic sized arrays like vector in C++,
  list in Python, ArrayList in Java, stacks can grow and shrink with
  array implementation as well].
* The total size of the stack must be defined beforehand.

Join
[GfG 160](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium), a 160-day journey of coding challenges aimed at sharpening your
skills. Each day, solve a handpicked problem, dive into detailed
solutions through articles and videos, and enhance your preparation for
any interview—all for free! Plus, win exciting GfG goodies along the
way! -
[Explore Now](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium)
', 'Stack is a linear data structure which follows LIFO principle In this article we will learn how to implement Stack usingArrays In Arraybased approach all stackrelated operations areexecuted using arrays Lets see how we can implement each operation onthe stack utilizing the Array Data Structure', 'Array Implementation', 2, '7f481263-762a-4311-94d3-fb3aeed36154', null);
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('05a6bc0c-6e95-4f5e-880f-284b1618488d', e'So we need to follow a simple rule in the implementation of a stack
which is ****last in first out****
and all the operations can be performed with the help of a top variable.
Let us learn how to perform ****Pop, Push, Peek, and Display**** operations in the following article:

![](https://media.geeksforgeeks.org/wp-content/uploads/1-354.png)![](https://media.geeksforgeeks.org/wp-content/uploads/2-260.png)![](https://media.geeksforgeeks.org/wp-content/uploads/3-186.png)

In the stack Implementation, a stack contains a top pointer. which is
the “head” of the stack where pushing and popping items happens at the
head of the list. The first node has a null in the link field and second
node-link has the first node address in the link field and so on and the
last node address is in the “top” pointer.

The main advantage of using a linked list over arrays is that it is
possible to implement a stack that can shrink or grow as much as needed.
Using an array will put a restriction on the maximum capacity of the
array which can lead to stack overflow. Here each new node will be
dynamically allocated. so overflow is not possible.

****Stack Operations:****
-------------------------

* [****push()****](https://www.geeksforgeeks.org/stack-push-and-pop-in-c-stl/)****:****
  Insert a new element into the stack i.e just insert a new element at
  the beginning of the linked list.
* [****pop()****](https://www.geeksforgeeks.org/stack-push-and-pop-in-c-stl/)****:****
  Return the top element of the Stack i.e simply delete the first
  element from the linked list.
* [****peek()****](https://www.geeksforgeeks.org/stack-peek-method-in-java/)****:**** Return the top element.
* ****display():**** Print all elements in Stack.

Push Operation:
---------------

> * Initialise a node
> * Update the value of that node by data i.e. ****node->data = data****
> * Now link this node to the top of the linked list
> * And update top pointer to the current node

Pop Operation:
--------------

> * First Check whether there is any node present in the linked list or
>   not, if not then return
> * Otherwise make pointer let say ****temp**** to the top node and move forward the top node by 1 step
> * Now free this temp node

Peek Operation:
---------------

> * Check if there is any node present or not, if not then
>   return.
> * Otherwise return the value of top node of the linked list

Display Operation:
------------------

> * Take a ****temp**** node and initialize it with top pointer
> * Now start traversing temp till it encounters NULL
> * Simultaneously print the value of the temp node



Below is the implementation of the above operations

C++
````
// C++ program to implement a stack using singly linked list
#include <bits/stdc++.h>
using namespace std;

// Class representing a node in the linked list
class Node {
public:
    int data;
    Node* next;
    Node(int new_data) {
        this->data = new_data;
        this->next = nullptr;
    }
};

// Class to implement stack using a singly linked list
class Stack {

    // head of the linked list
    Node* head;

public:
    // Constructor to initialize the stack
    Stack() { this->head = nullptr; }

    // Function to check if the stack is empty
    bool isEmpty() {

        // If head is nullptr, the stack is empty
        return head == nullptr;
    }

    // Function to push an element onto the stack
    void push(int new_data) {

        // Create a new node with given data
        Node* new_node = new Node(new_data);

        // Check if memory allocation for the new node
        // failed
        if (!new_node) {
            cout << "\\nStack Overflow";
        }

        // Link the new node to the current top node
        new_node->next = head;

        // Update the top to the new node
        head = new_node;
    }

    // Function to remove the top element from the stack
    void pop() {

        // Check for stack underflow
        if (this->isEmpty()) {
            cout << "\\nStack Underflow" << endl;
        }
        else {
            // Assign the current top to a temporary
            // variable
            Node* temp = head;

            // Update the top to the next node
            head = head->next;

            // Deallocate the memory of the old top node
            delete temp;
        }
    }

    // Function to return the top element of the stack
    int peek() {

        // If stack is not empty, return the top element
        if (!isEmpty())
            return head->data;
        else {
            cout << "\\nStack is empty";
            return INT_MIN;
        }
    }
};

// Driver program to test the stack implementation
int main() {
    // Creating a stack
    Stack st;

    // Push elements onto the stack
    st.push(11);
    st.push(22);
    st.push(33);
    st.push(44);

    // Print top element of the stack
    cout << "Top element is " << st.peek() << endl;

    // removing two elemements from the top
      cout << "Removing two elements..." << endl;
    st.pop();
    st.pop();

    // Print top element of the stack
    cout << "Top element is " << st.peek() << endl;

    return 0;
}

````

C
````
// C program to implement a stack using singly linked list
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>

// Struct representing a node in the linked list
typedef struct Node {
    int data;
    struct Node* next;
} Node;
Node* createNode(int new_data) {
    Node* new_node = (Node*)malloc(sizeof(Node));
    new_node->data = new_data;
    new_node->next = NULL;
    return new_node;
}

// Struct to implement stack using a singly linked list
typedef struct Stack {
    Node* head;
} Stack;

// Constructor to initialize the stack
void initializeStack(Stack* stack) { stack->head = NULL; }

// Function to check if the stack is empty
int isEmpty(Stack* stack) {

    // If head is NULL, the stack is empty
    return stack->head == NULL;
}

// Function to push an element onto the stack
void push(Stack* stack, int new_data) {

    // Create a new node with given data
    Node* new_node = createNode(new_data);

    // Check if memory allocation for the new node failed
    if (!new_node) {
        printf("\\nStack Overflow");
        return;
    }

    // Link the new node to the current top node
    new_node->next = stack->head;

    // Update the top to the new node
    stack->head = new_node;
}

// Function to remove the top element from the stack
void pop(Stack* stack) {

    // Check for stack underflow
    if (isEmpty(stack)) {
        printf("\\nStack Underflow\\n");
        return;
    }
    else {

        // Assign the current top to a temporary variable
        Node* temp = stack->head;

        // Update the top to the next node
        stack->head = stack->head->next;

        // Deallocate the memory of the old top node
        free(temp);
    }
}

// Function to return the top element of the stack
int peek(Stack* stack) {

    // If stack is not empty, return the top element
    if (!isEmpty(stack))
        return stack->head->data;
    else {
        printf("\\nStack is empty");
        return INT_MIN;
    }
}

// Driver program to test the stack implementation
int main() {

    // Creating a stack
    Stack stack;
    initializeStack(&stack);

    // Push elements onto the stack
    push(&stack, 11);
    push(&stack, 22);
    push(&stack, 33);
    push(&stack, 44);

    // Print top element of the stack
    printf("Top element is %d\\n", peek(&stack));


      // removing two elemements from the top
      printf("Removing two elements...\\n");
    pop(&stack);
    pop(&stack);

    // Print top element of the stack
    printf("Top element is %d\\n", peek(&stack));

    return 0;
}

````

Java
````
// Java program to implement a stack using singly linked
// list

// Class representing a node in the linked list
class Node {
    int data;
    Node next;
    Node(int new_data) {
        this.data = new_data;
        this.next = null;
    }
}

// Class to implement stack using a singly linked list
class Stack {

    // Head of the linked list
    Node head;

    // Constructor to initialize the stack
    Stack() { this.head = null; }

    // Function to check if the stack is empty
    boolean isEmpty() {

        // If head is null, the stack is empty
        return head == null;
    }

    // Function to push an element onto the stack
    void push(int new_data) {

        // Create a new node with given data
        Node new_node = new Node(new_data);

        // Check if memory allocation for the new node
        // failed
        if (new_node == null) {
            System.out.println("\\nStack Overflow");
            return;
        }

        // Link the new node to the current top node
        new_node.next = head;

        // Update the top to the new node
        head = new_node;
    }

    // Function to remove the top element from the stack
    void pop() {

        // Check for stack underflow
        if (isEmpty()) {
            System.out.println("\\nStack Underflow");
            return;
        }
        else {

            // Assign the current top to a temporary
            // variable
            Node temp = head;

            // Update the top to the next node
            head = head.next;

            // Deallocate the memory of the old top node
            temp = null;
        }
    }

    // Function to return the top element of the stack
    int peek() {

        // If stack is not empty, return the top element
        if (!isEmpty())
            return head.data;
        else {
            System.out.println("\\nStack is empty");
            return Integer.MIN_VALUE;
        }
    }
}

// Driver code
public class Main {
    public static void main(String[] args)
    {
        // Creating a stack
        Stack st = new Stack();

        // Push elements onto the stack
        st.push(11);
        st.push(22);
        st.push(33);
        st.push(44);

        // Print top element of the stack
        System.out.println("Top element is " + st.peek());

        // removing two elemements from the top
          System.out.println("Removing two elements...");
        st.pop();
        st.pop();

        // Print top element of the stack
        System.out.println("Top element is " + st.peek());
    }
}

````

Python
````
# Java program to implement a stack using singly linked
# list

# Class representing a node in the linked list
class Node:
    def __init__(self, new_data):
        self.data = new_data
        self.next = None

# Class to implement stack using a singly linked list
class Stack:
    def __init__(self):

        # head of the linked list
        self.head = None

    # Function to check if the stack is empty
    def is_empty(self):

        # If head is None, the stack is empty
        return self.head is None

    # Function to push an element onto the stack
    def push(self, new_data):

        # Create a new node with given data
        new_node = Node(new_data)

        # Check if memory allocation for the new node failed
        if not new_node:
            print("\\nStack Overflow")
            return

        # Link the new node to the current top node
        new_node.next = self.head

        # Update the top to the new node
        self.head = new_node

    # Function to remove the top element from the stack
    def pop(self):

        # Check for stack underflow
        if self.is_empty():
            print("\\nStack Underflow")
        else:

            # Assign the current top to a temporary variable
            temp = self.head

            # Update the top to the next node
            self.head = self.head.next

            # Deallocate the memory of the old top node
            del temp

    # Function to return the top element of the stack
    def peek(self):

        # If stack is not empty, return the top element
        if not self.is_empty():
            return self.head.data
        else:
            print("\\nStack is empty")
            return float("-inf")


# Creating a stack
st = Stack()

# Push elements onto the stack
st.push(11)
st.push(22)
st.push(33)
st.push(44)

# Print top element of the stack
print("Top element is", st.peek())

# removing two elemements from the top
print("Removing two elements...");
st.pop()
st.pop()

# Print top element of the stack
print("Top element is", st.peek())

````

C#
````
// C# program to implement a stack using singly linked list
using System;

// Class representing a node in the linked list
class Node {
    public int data;
    public Node next;
    public Node(int new_data)
    {
        this.data = new_data;
        this.next = null;
    }
}

// Class to implement stack using a singly linked list
class Stack {

    // head of the linked list
    private Node head;

    // Constructor to initialize the stack
    public Stack() { this.head = null; }

    // Function to check if the stack is empty
    public bool isEmpty()
    {

        // If head is null, the stack is empty
        return head == null;
    }

    // Function to push an element onto the stack
    public void push(int new_data)
    {

        // Create a new node with given data
        Node new_node = new Node(new_data);

        // Check if memory allocation for the new node
        // failed
        if (new_node == null) {
            Console.WriteLine("\\nStack Overflow");
            return;
        }

        // Link the new node to the current top node
        new_node.next = head;

        // Update the top to the new node
        head = new_node;
    }

    // Function to remove the top element from the stack
    public void pop()
    {

        // Check for stack underflow
        if (this.isEmpty()) {
            Console.WriteLine("\\nStack Underflow");
        }
        else {

            // Update the top to the next node
            head = head.next;
            /* No need to manually free the memory of the
             * old head in C# */
        }
    }

    // Function to return the top element of the stack
    public int peek()
    {

        // If stack is not empty, return the top element
        if (!isEmpty())
            return head.data;
        else {
            Console.WriteLine("\\nStack is empty");
            return int.MinValue;
        }
    }
}

// Driver program to test the stack implementation
class GfG {
    static void Main(string[] args)
    {

        // Creating a stack
        Stack st = new Stack();

        // Push elements onto the stack
        st.push(11);
        st.push(22);
        st.push(33);
        st.push(44);

        // Print top element of the stack
        Console.WriteLine("Top element is " + st.peek());

        // removing two elemements from the top
          Console.WriteLine("Removing two elements...");
        st.pop();
        st.pop();

        // Print top element of the stack
        Console.WriteLine("Top element is " + st.peek());
    }
}

````

JavaScript
````
// Javascript program to implement a stack using singly
// linked list

// Class representing a node in the linked list
class Node {
    constructor(new_data) {
        this.data = new_data;
        this.next = null;
    }
}

// Class to implement stack using a singly linked list
class Stack {

    // Constructor to initialize the stack
    constructor() { this.head = null; }

    // Function to check if the stack is empty
    isEmpty() {

        // If head is null, the stack is empty
        return this.head === null;
    }

    // Function to push an element onto the stack
    push(new_data) {

        // Create a new node with given data
        const new_node = new Node(new_data);

        // Check if memory allocation for the new node
        // failed
        if (!new_node) {
            console.log("\\nStack Overflow");
            return;
        }

        // Link the new node to the current top node
        new_node.next = this.head;

        // Update the top to the new node
        this.head = new_node;
    }

    // Function to remove the top element from the stack
    pop() {

        // Check for stack underflow
        if (this.isEmpty()) {
            console.log("\\nStack Underflow");
        }
        else {

            // Assign the current top to a temporary
            // variable
            let temp = this.head;

            // Update the top to the next node
            this.head = this.head.next;

            // Deallocate the memory of the old top node
            temp = null;
        }
    }

    // Function to return the top element of the stack
    peek() {

        // If stack is not empty, return the top element
        if (!this.isEmpty())
            return this.head.data;
        else {
            console.log("\\nStack is empty");
            return Number.MIN_VALUE;
        }
    }
}

// Driver program to test the stack implementation
const st = new Stack();

// Push elements onto the stack
st.push(11);
st.push(22);
st.push(33);
st.push(44);

// Print top element of the stack
console.log("Top element is " + st.peek());

// removing two elemements from the top
console.log("Removing two elements...");
st.pop();
st.pop();

// Print top element of the stack
console.log("Top element is " + st.peek());

````


**Output**
```

Top element is 44
Top element is 22

```

****Time Complexity:****
O(1), for all push(), pop(), and peek(), as we are not performing any
kind of traversal over the list. We perform all the operations through
the current pointer only.
****Auxiliary Space:**** O(N), where N is the size of the stack


In this implementation, we define a Node class that represents a node
in the linked list, and a Stack class that uses this node class to
implement the stack. The head attribute of the Stack class points to the
top of the stack (i.e., the first node in the linked list).

To push an item onto the stack, we create a new node with the given
item and set its next pointer to the current head of the stack. We then
set the head of the stack to the new node, effectively making it the new
top of the stack.

To pop an item from the stack, we simply remove the first node from the
linked list by setting the head of the stack to the next node in the
list (i.e., the node pointed to by the next pointer of the current
head). We return the data stored in the original head node, which is the
item that was removed from the top of the stack.

### Benefits of implementing a stack using a singly linked list include:

****Dynamic memory allocation****: The size of the stack can be increased or decreased dynamically by
adding or removing nodes from the linked list, without the need to
allocate a fixed amount of memory for the stack upfront.

****Efficient memory usage:**** Since nodes in a singly linked list only have a next pointer and not a
prev pointer, they use less memory than nodes in a doubly linked
list.

****Easy implementation****: Implementing a stack using a singly linked list is straightforward
and can be done using just a few lines of code.

****Versatile****: Singly linked lists can be used to implement other data structures
such as queues, linked lists, and trees.

In summary, implementing a stack using a singly linked list is a simple
and efficient way to create a dynamic stack data structure in
Python.

### Real time examples of stack:

Stacks are used in various real-world scenarios where a last-in,
first-out (LIFO) data structure is required. Here are some examples of
real-time applications of stacks:

****Function call stack****: When a function is called in a program, the return address and all
the function parameters are pushed onto the function call stack. The
stack allows the function to execute and return to the caller function
in the reverse order in which they were called.

****Undo/Redo operations:****
In many applications, such as text editors, image editors, or web
browsers, the undo and redo functionalities are implemented using a
stack. Every time an action is performed, it is pushed onto the stack.
When the user wants to undo the last action, the top element of the
stack is popped and the action is reversed.

****Browser history:**** Web browsers use stacks to keep track of the pages visited by the user.
Every time a new page is visited, its URL is pushed onto the stack. When
the user clicks the “Back” button, the last visited URL is popped from
the stack and the user is directed to the previous page.

****Expression evaluation****: Stacks are used in compilers and interpreters to evaluate
expressions. When an expression is parsed, it is converted into postfix
notation and pushed onto a stack. The postfix expression is then
evaluated using the stack.

****Call stack in recursion:****
When a recursive function is called, its call is pushed onto the stack.
The function executes and calls itself, and each subsequent call is
pushed onto the stack. When the recursion ends, the stack is popped, and
the program returns to the previous function call.

In summary, stacks are widely used in many applications where LIFO
functionality is required, such as function calls, undo/redo operations,
browser history, expression evaluation, and recursive function
calls.

Join
[GfG 160](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium), a 160-day journey of coding challenges aimed at sharpening your
skills. Each day, solve a handpicked problem, dive into detailed
solutions through articles and videos, and enhance your preparation for
any interview—all for free! Plus, win exciting GfG goodies along the
way! -
[Explore Now](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium)
', 'To implement a stack structure using the singly linked list concept all the singly linked list operations should be performed based on Stack operations LIFO last infirst out and with the help of that knowledge we are going toimplement a stack using a singly linked list ', 'Linked List Implementation', 3, '7f481263-762a-4311-94d3-fb3aeed36154', null);
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('433daf24-4f76-4dd9-934e-09edfb0afc1e', e'Queue Data Structure

### **Operation 1: enqueue()**

Inserts an element at the end of the queue i.e. at the rear end.

The following steps should be taken to enqueue (insert) data into a queue:

* Check if the queue is full.
* If the queue is full, return overflow error and exit.
* If the queue is not full, increment the rear pointer to point to the
  next empty space.
* Add the data element to the queue location, where the rear is pointing.
* return success.

![Enqueue representation](https://media.geeksforgeeks.org/wp-content/uploads/20220805122158/fifo1-660x371.png)

Enqueue representation

Below is the Implementation of the above approach:

* C++
* Java
* C
* Python3
* C#
* Javascript

C++
---












| `void` `queueEnqueue(``int` `data)`  `{`  `// Check queue is full or not`  `if` `(capacity == rear) {`  `printf``(``"\\nQueue is full\\n"``);`  `return``;`  `}`    `// Insert element at the rear`  `else` `{`  `queue[rear] = data;`  `rear++;`  `}`  `return``;`  `}` |
| --- |





Java
----












| `void` `queueEnqueue(``int` `data)`  `{`  `// Check queue is full or not`  `if` `(capacity == rear) {`  `System.out.println(``"\\nQueue is full\\n"``);`  `return``;`  `}`    `// Insert element at the rear`  `else` `{`  `queue[rear] = data;`  `rear++;`  `}`  `return``;`  `}`    `// This code is contributed by aadityapburujwale` |
| --- |





C
-












| `// Function to add an item to the queue.`  `// It changes rear and size`  `void` `enqueue(``struct` `Queue* queue,` `int` `item)`  `{`  `if` `(isFull(queue))`  `return``;`  `queue->rear = (queue->rear + 1) % queue->capacity;`  `queue->array[queue->rear] = item;`  `queue->size = queue->size + 1;`  `printf``(``"%d enqueued to queue\\n"``, item);`  `}`    `// This code is contributed by Susobhan Akhuli` |
| --- |





Python3
-------












| `# Function to add an item to the queue.`  `# It changes rear and size`  `def` `EnQueue(``self``, item):`  `if` `self``.isFull():`  `print``(``"Full"``)`  `return`  `self``.rear` `=` `(``self``.rear` `+` `1``)` `%` `(``self``.capacity)`  `self``.Q[``self``.rear]` `=` `item`  `self``.size` `=` `self``.size` `+` `1`  `print``(``"% s enqueued to queue"` `%` `str``(item))`  `# This code is contributed by Susobhan Akhuli` |
| --- |





C#
--












| `// Function to add an item to the queue.`  `// It changes rear and size`  `public` `void` `enqueue(``int` `item)`  `{`  `if` `(rear == max - 1) {`  `Console.WriteLine(``"Queue Overflow"``);`  `return``;`  `}`  `else` `{`  `ele[++rear] = item;`  `}`  `}`    `// This code is contributed by Susobhan Akhuli` |
| --- |





Javascript
----------












| `<script>`  `enqueue(element){`  `// adding element to the queue`  `this``.items.push(element);`  `}`    `// This code is contributed by Susobhan Akhuli`  `</script>` |
| --- |








**Complexity Analysis:**
**Time Complexity:** O(1)
**Space Complexity:** O(N)

### **Operation 2: dequeue()**

This operation removes and returns an element that is at the front end of
the queue.

The following steps are taken to perform the dequeue operation:

* Check if the queue is empty.
* If the queue is empty, return the underflow error and exit.
* If the queue is not empty, access the data where the front is pointing.
* Increment the front pointer to point to the next available data element.
* The Return success.

![Dequeue operation](https://media.geeksforgeeks.org/wp-content/uploads/20220805122625/fifo2-660x371.png)

Dequeue operation

Below is the Implementation of above approach:

* C++
* Java
* C
* Python3
* C#
* Javascript

C++
---












| `void` `queueDequeue()`  `{`  `// If queue is empty`  `if` `(front == rear) {`  `printf``(``"\\nQueue is empty\\n"``);`  `return``;`  `}`    `// Shift all the elements from index 2`  `// till rear to the left by one`  `else` `{`  `for` `(``int` `i = 0; i < rear - 1; i++) {`  `queue[i] = queue[i + 1];`  `}`    `// decrement rear`  `rear--;`  `}`  `return``;`  `}` |
| --- |





Java
----












| `void` `queueDequeue()`  `{`  `// If queue is empty`  `if` `(front == rear) {`  `System.out.println(``"\\nQueue is empty\\n"``);`  `return``;`  `}`    `// Shift all the elements from index 2`  `// till rear to the left by one`  `else` `{`  `for` `(``int` `i =` `0``; i < rear -` `1``; i++) {`  `queue[i] = queue[i +` `1``];`  `}`    `// decrement rear`  `rear--;`  `}`  `return``;`  `}`    `// This code is contributed by aadityapburujwale` |
| --- |





C
-












| `// Function to remove an item from queue.`  `// It changes front and size`  `int` `dequeue(``struct` `Queue* queue)`  `{`  `if` `(isEmpty(queue)) {`  `printf``(``"\\nQueue is empty\\n"``);`  `return``;`  `}`  `int` `item = queue->array[queue->front];`  `queue->front = (queue->front + 1) % queue->capacity;`  `queue->size = queue->size - 1;`  `return` `item;`  `}`    `// This code is contributed by Susobhan Akhuli` |
| --- |





Python3
-------












| `# Function to remove an item from queue.`  `# It changes front and size`      `def` `DeQueue(``self``):`  `if` `self``.isEmpty():`  `print``(``"Queue is empty"``)`  `return`    `print``(``"% s dequeued from queue"` `%` `str``(``self``.Q[``self``.front]))`  `self``.front` `=` `(``self``.front` `+` `1``)` `%` `(``self``.capacity)`  `self``.size` `=` `self``.size` `-` `1`  `# This code is contributed by Susobhan Akhuli` |
| --- |





C#
--












| `// Function to remove an item from queue.`  `// It changes front and size`  `public` `int` `dequeue()`  `{`  `if` `(front == rear + 1) {`  `Console.WriteLine(``"Queue is Empty"``);`  `return` `-1;`  `}`  `else` `{`  `int` `p = ele[front++];`  `return` `p;`  `}`  `}`  `// This code is contributed by Susobhan Akhuli` |
| --- |





Javascript
----------












| `<script>`  `dequeue(){`  `// removing element from the queue`  `// returns underflow when called`  `// on empty queue`  `if``(``this``.isEmpty()){`  `document.write(``"<br>Queue is empty<br>"``);`  `return` `-1;`  `}`  `return` `this``.items.shift();`  `}`  `// This code is contributed by Susobhan Akhuli`  `</script>` |
| --- |








**Complexity Analysis:**
**Time Complexity:** O(1)
**Space Complexity:** O(N)

### **Operation 3: front()**

This operation returns the element at the front end without removing it.

The following steps are taken to perform the front operation:

* If the queue is empty return the most minimum value.
* otherwise, return the front value.

Below is the Implementation of the above approach:

* C++
* Java
* C
* Python3
* C#
* Javascript

C++
---












| `// Function to get front of queue`  `int` `front(Queue* queue)`  `{`  `if` `(isempty(queue))`  `return` `INT_MIN;`  `return` `queue->arr[queue->front];`  `}` |
| --- |





Java
----












| `// Function to get front of queue`  `int` `front(Queue queue)`  `{`  `if` `(isempty(queue))`  `return` `Integer.MIN_VALUE;`  `return` `queue.arr[queue.front];`  `}`    `// This code is contributed by aadityapburujwale` |
| --- |





C
-












| `// Function to get front of queue`  `int` `front(``struct` `Queue* queue)`  `{`  `if` `(isempty(queue))`  `return` `INT_MIN;`  `return` `queue->arr[queue->front];`  `}`    `// This code is contributed by Susobhan Akhuli` |
| --- |





Python3
-------












| `# Function to get front of queue`  `def` `que_front(``self``):`  `if` `self``.isempty():`  `return` `"Queue is empty"`  `return` `self``.Q[``self``.front]`    `# This code is contributed By Susobhan Akhuli` |
| --- |





C#
--












| `// Function to get front of queue`  `public` `int` `front()`  `{`  `if` `(isempty())`  `return` `INT_MIN;`  `return` `arr[front];`  `}`    `// This code is contributed By Susobhan Akhuli` |
| --- |





Javascript
----------












| `<script>`  `// Function to get front of queue`  `front(){`  `// returns the Front element of`  `// the queue without removing it.`  `if``(``this``.isEmpty())`  `return` `"No elements in Queue<br>"``;`  `return` `this``.items[0];`  `}`  `// This code is contributed By Susobhan Akhuli`  `<script>` |
| --- |








**Complexity Analysis:**
**Time Complexity:** O(1)
**Space Complexity:** O(N)

### Operation 4 : rear()

This operation returns the element at the rear end without removing it.

The following steps are taken to perform the rear operation:

* If the queue is empty return the most minimum value.
* otherwise, return the rear value.

Below is the Implementation of the above approach:

* C++
* Java
* C
* Python3
* C#
* Javascript

C++
---












| `// Function to get rear of queue`  `int` `rear(Queue* queue)`  `{`  `if` `(isEmpty(queue))`  `return` `INT_MIN;`  `return` `queue->arr[queue->rear];`  `}` |
| --- |





Java
----












| `// Function to get rear of queue`  `int` `rear(Queue queue)`  `{`  `if` `(isEmpty(queue))`  `return` `Integer.MIN_VALUE;`  `return` `queue.arr[queue.rear];`  `}`    `// This code is contributed by aadityapburujwale` |
| --- |





C
-












| `// Function to get front of queue`  `int` `front(``struct` `Queue* queue)`  `{`  `if` `(isempty(queue))`  `return` `INT_MIN;`  `return` `queue->arr[queue->rear];`  `}`    `// This code is contributed by Susobhan Akhuli` |
| --- |

Python3
-------

| `# Function to get rear of queue`  `def` `que_rear(``self``):`  `if` `self``.isEmpty():`  `return` `"Queue is empty"`  `return` `self``.Q[``self``.rear]`    `# This code is contributed By Susobhan Akhuli` |
| --- |

C#
--
| `// Function to get front of queue`  `public` `int` `front()`  `{`  `if` `(isempty())`  `return` `INT_MIN;`  `return` `arr[rear];`  `}`    `// This code is contributed By Susobhan Akhuli` |
| --- |

Javascript
----------


| `<script>`  `rear(){`  `// returns the Rear element of`  `// the queue without removing it.`  `if``(``this``.isEmpty())`  `return` `"No elements in Queue<br>"``;`  `return` `this``.items[``this``.items.length-1];`  `}`  `// This code is contributed By Susobhan Akhuli`  `<script>` |
| --- |








**Complexity Analysis:**
**Time Complexity:** O(1)
**Space Complexity:** O(N)

### **Operation 5: isEmpty():**

This operation returns a boolean value that indicates whether the queue is
empty or not.

The following steps are taken to perform the Empty operation:

* check if front value is equal to -1 or not, if yes then return true
  means queue is empty.
* Otherwise return false, means queue is not empty

Below is the implementation of the above approach:

* C++
* Java
* C#
* C
* Python3
* Javascript
C++
---
| `// This function will check whether`  `// the queue is empty or not:`  `bool` `isEmpty()`  `{`  `if` `(front == -1)`  `return` `true``;`  `else`  `return` `false``;`  `}` |
| --- |
Java
----
| `// This function will check whether`  `// the queue is empty or not:`  `boolean` `isEmpty()`  `{`  `if` `(front == -``1``)`  `return` `true``;`  `else`  `return` `false``;`  `}`    `// This code is contributed by aadityapburujwale` |
| --- |
C#
--
| `// This function will check whether`  `// the queue is empty or not:`  `bool` `isEmpty()`  `{`  `if` `(front == -1)`  `return` `true``;`  `else`  `return` `false``;`  `}`    `// This code is contributed by lokeshmvs21.` |
| --- |
C
-
| `// Queue is empty when size is 0`  `bool` `isEmpty(``struct` `Queue* queue)`  `{`  `return` `(queue->size == 0);`  `}`    `// This code is contributed by Susobhan Akhuli` |
| --- |
Python3
-------
| `# Queue is empty when size is 0`  `def` `isEmpty(``self``):`  `return` `self``.size` `=``=` `0`  `# This code is contributed by Susobhan Akhuli` |
| --- |
Javascript
----------
| `</script>`  `isEmpty(){`  `// return true if the queue is empty.`  `return` `this``.items.length == 0;`  `}`  `// This code is contributed by Susobhan Akhuli`  `</script>` |
| --- |
**Complexity Analysis:**
**Time Complexity:** O(1)
**Space Complexity:** O(N)

### **Operation 6 : isFull()**

This operation returns a boolean value that indicates whether the queue is
full or not.

The following steps are taken to perform the isFull() operation:

* Check if front value is equal to zero and rear is equal to the capacity
  of queue if yes then return true.
* otherwise return false

Below is the Implementation of the above approach:

* C++
* Java
* C
* C#
* Python3
* Javascript

C++
---
| `// This function will check`  `// whether the queue is full or not.`  `bool` `isFull()`  `{`  `if` `(front == 0 && rear == MAX_SIZE - 1) {`  `return` `true``;`  `}`  `return` `false``;`  `}` |
| --- |
Java
----
| `// This function will check`  `// whether the queue is full or not.`  `boolean` `isFull()`  `{`  `if` `(front ==` `0` `&& rear == MAX_SIZE -` `1``) {`  `return` `true``;`  `}`  `return` `false``;`  `}`    `// This code is contributed by aadityapburujwale` |
| --- |
C
--
| `// Queue is full when size becomes`  `// equal to the capacity`  `bool` `isFull(``struct` `Queue* queue)`  `{`  `return` `(queue->size == queue->capacity);`  `}`    `// This code is contributed by Susobhan Akhuli` |
| --- |
C#
--
| `// Function to add an item to the queue.`  `// It changes rear and size`  `public` `bool` `isFull(``int` `item) {` `return` `(rear == max - 1); }`  `// This code is contributed by Susobhan Akhuli` |
| --- |
Python3
-------
| `# Queue is full when size becomes`  `# equal to the capacity`      `def` `isFull(``self``):`  `return` `self``.size` `=``=` `self``.capacity`    `# This code is contributed by Susobhan Akhuli` |
| --- |
Javascript
----------
| `function` `isFull() {`  `if` `(front == 0 && rear == MAX_SIZE - 1) {`  `return` `true``;`  `}`  `return` `false``;`  `}`    `// This code is contributed by aadityamaharshi21.` |
| --- |
**Complexity Analysis:**
**Time Complexity:** O(1)
**Space Complexity:** O(N)

### Operation 7: size()

This operation returns the size of the queue i.e. the total number of
elements it contains.

```
queuename.size()
Parameters :
No parameters are passed
Returns :
Number of elements in the container
```

* C++
* Java
* Python
* C#
* Javascript

C++
---
| `// CPP program to illustrate`  `// Implementation of size() function`  `#include <iostream>`  `#include <queue>`  `using` `namespace` `std;`    `int` `main()`  `{`  `int` `sum = 0;`  `queue<``int``> myqueue;`  `myqueue.push(1);`  `myqueue.push(8);`  `myqueue.push(3);`  `myqueue.push(6);`  `myqueue.push(2);`    `// Queue becomes 1, 8, 3, 6, 2`    `cout << myqueue.size();`    `return` `0;`  `}` |
| --- |
Java
----
| `// Java program to illustrate implementation of size()`  `// function`    `import` `java.util.*;`    `public` `class` `Main {`  `public` `static` `void` `main(String[] args)`  `{`  `int` `sum =` `0``;`  `Queue<Integer> myqueue =` `new` `LinkedList<>();`  `myqueue.add(``1``);`  `myqueue.add(``8``);`  `myqueue.add(``3``);`  `myqueue.add(``6``);`  `myqueue.add(``2``);`    `// Queue becomes 1, 8, 3, 6, 2`    `System.out.println(myqueue.size());`  `}`  `}`    `// This code is contributed by lokesh.` |
| --- |
Python
------

| `from` `collections` `import` `deque`    `def` `main():`  `sum` `=` `0`  `myqueue` `=` `deque()`  `myqueue.append(``1``)`  `myqueue.append(``8``)`  `myqueue.append(``3``)`  `myqueue.append(``6``)`  `myqueue.append(``2``)`    `# Queue becomes 1, 8, 3, 6, 2`    `print``(``len``(myqueue))`    `main()`    `# This code is contributed by aadityamaharshi21.` |
| --- |

C#
--

| `using` `System;`  `using` `System.Collections.Generic;`    `namespace` `ConsoleApp1 {`  `class` `MainClass {`  `public` `static` `void` `Main(``string``[] args)`  `{`  `int` `sum = 0;`  `Queue<``int``> myqueue =` `new` `Queue<``int``>();`  `myqueue.Enqueue(1);`  `myqueue.Enqueue(8);`  `myqueue.Enqueue(3);`  `myqueue.Enqueue(6);`  `myqueue.Enqueue(2);`    `// Queue becomes 1, 8, 3, 6, 2`    `Console.WriteLine(myqueue.Count);`  `}`  `}`  `}`    `// This code is contributed by adityamaharshi21.` |
| --- |

Javascript
----------

| `// Javascript code`  `let sum = 0;`  `let myqueue=[];`  `myqueue.push(1);`  `myqueue.push(8);`  `myqueue.push(3);`  `myqueue.push(6);`  `myqueue.push(2);`    `// Queue becomes 1, 8, 3, 6, 2`    `console.log(myqueue.length);` |
| --- |

**Complexity Analysis:**
**Time Complexity:** O(1)
**Space Complexity:** O(N)

Join
[GfG 160](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium), a 160-day journey of coding challenges aimed at sharpening your
skills. Each day, solve a handpicked problem, dive into detailed
solutions through articles and videos, and enhance your preparation for
any interview—all for free! Plus, win exciting GfG goodies along the
way! -
[Explore Now](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium)', 'Basic Operations on QueueSome of the basic operations for Queue in Data Structure are enqueue  Insertion of elements to the queue dequeue  Removal of elements from the queue peek or front Acquires the data element available  at the front node of the queue without deleting it rear  This operation returns the element at the rear  end without removing it isFull  Validates if the queue is full isEmpty  Checks if the queue is empty size This operation returns the size of the queue  ie the total number of elements it contains', 'Basic Operations for Queue in Data Structure', 2, '940fddca-cb93-47d1-a28f-355e3c4490d2', null);
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('57716d79-f039-4789-8d63-7052511a0058', e'What is a Doubly Linked List?
-----------------------------

A ****doubly linked list****
is a data structure that consists of a set of nodes, each of which
contains a ****value**** and ****two pointers****, one pointing to the ****previous node**** in the list and one pointing to the ****next node****
in the list. This allows for efficient traversal of the list in ****both directions****, making it suitable for applications where frequent ****insertions**** and ****deletions**** are required.

![Insertion-at-the-End-in-Doubly-Linked-List-copy](https://media.geeksforgeeks.org/wp-content/uploads/20240809123741/Insertion-at-the-End-in-Doubly-Linked-List-copy.webp)

Doubly Linked List


Representation of Doubly Linked List in Data Structure
------------------------------------------------------

In a data structure, a doubly linked list is represented using nodes
that have three fields:

1. Data
2. A pointer to the next node (****next****)
3. A pointer to the previous node (****prev****)

![Node-Structure-of-Doubly-Linked-List](https://media.geeksforgeeks.org/wp-content/uploads/20240809124907/Node-Structure-of-Doubly-Linked-List.webp)

Node Structure of Doubly Linked List


Node Definition
---------------

Here is how a node in a Doubly Linked List is typically
represented:

[Try it on GfG Practice
![redirect icon](https://media.geeksforgeeks.org/auth-dashboard-uploads/Group-arrow.svg)](https://www.geeksforgeeks.org/problems/display-doubly-linked-list--154650/1?itm_source=geeksforgeeks&itm_medium=article&itm_campaign=practice_card)
C++
````
struct Node {

    // To store the Value or data.
    int data;

    // Pointer to point the Previous Element
    Node* prev;

    // Pointer to point the Next Element
    Node* next;

    // Constructor
    Node(int d) {
       data = d;
       prev = next = nullptr;
    }
};

````

C
````
struct Node {

    // To store the Value or data.
    int data;

    // Pointer to point the Previous Element
    Node* prev;

    // Pointer to point the Next Element
    Node* next;
};

// Function to create a new node
struct Node *createNode(int new_data) {
    struct Node *new_node = (struct Node *)
    malloc(sizeof(struct Node));
    new_node->data = new_data;
    new_node->next = NULL;
    new_node->prev = NULL;
    return new_node;
}

````

Java
````
class Node {

    // To store the Value or data.
    int data;

    // Reference to the Previous Node
    Node prev;

    // Reference to the next Node
    Node next;

    // Constructor
    Node(int d) {
       data = d;
       prev = next = null;
    }
};

````', 'A doubly linked listis a more complex data structure than a singly linked list but itoffers several advantages The main advantage of a doubly linked list isthat it allows for efficient traversal of the list in both directionsThis is because each node in the list contains a pointer to the previousnode and a pointer to the next node This allows for quick and easyinsertion and deletion of nodes from the list as well as efficienttraversal of the list in both directions', 'Doubly Linked List', 2, '95a8a2cd-95c1-4ad8-9c8c-61b8dfe820c5', null);
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('cc954017-289d-4b95-ba30-a52c92e52904', e'What is a Circular Linked List?
-------------------------------

A ****circular linked list****
is a special type of linked list where all the nodes are connected to
form a circle. Unlike a regular linked list, which ends with a node
pointing to ****NULL****, the last node in a circular linked list points back to the first
node. This means that you can keep traversing the list without ever
reaching a ****NULL**** value.

Types of Circular Linked Lists
------------------------------

We can create a circular linked list from both [singly linked lists](https://www.geeksforgeeks.org/introduction-to-singly-linked-list/) and [doubly linked lists](https://www.geeksforgeeks.org/doubly-linked-list-tutorial-2/). So, circular linked list are basically of two types:

### 1. Circular Singly Linked List

In ****Circular Singly Linked List****, each node has just one pointer called the “****next****” pointer. The next pointer of ****last node**** points back to the ****first node**** and this results in forming a circle. In this type of Linked list we
can only move through the list in one direction.

![Representation-of-circular-linked-list](https://media.geeksforgeeks.org/wp-content/uploads/20240806130914/Representation-of-circular-linked-list.webp)

Representation of Circular Singly Linked List

### 2. Circular Doubly Linked List:

In ****circular doubly linked**** ****list,**** each node has two pointers ****prev**** and ****next,**** similar to doubly linked list. The ****prev**** pointer points to the previous node and the ****next**** points to the next node. Here, in addition to the ****last**** node storing the address of the first node, the ****first node**** will also store the address of the ****last node****.

![Representation-of-circular-doubly-linked-list](https://media.geeksforgeeks.org/wp-content/uploads/20240806145223/Representation-of-circular-doubly-linked-list.webp)

Representation of Circular Doubly Linked List

****Note:**** In this article, we will use the circular singly linked list to explain
the working of circular linked lists.

Representation of a Circular Singly Linked List
-----------------------------------------------

Let’s take a look on the structure of a circular linked list.



![Node-structure-of-circular-linked-list](https://media.geeksforgeeks.org/wp-content/uploads/20240806145414/Node-structure-of-circular-linked-list.webp)

Representation of a Circular Singly Linked List

### Create/Declare a Node of Circular Linked List

Syntax to Declare a Circular Linked List in Different Languages:


C++
````
// Node structure
struct Node {
    int data;
    Node* next;

    Node(int value){
        data = value;
        next = nullptr;
    }
};

````

C
````
// Node structure
struct Node
{
    int data;
    struct Node *next;
};

// Function to create a new node
struct Node *createNode(int value){

    // Allocate memory
    struct Node *newNode =
      (struct Node *)malloc(sizeof(struct Node));

    // Set the data
    newNode->data = value;

    // Initialize next to NULL
    newNode->next = NULL;

    // Return the new node
    return newNode;
}

````

Java
````
class Node {
    int data;
    Node next;

    Node(int data)
    {
        this.data = data;
        this.next = null;
    }
}

````

Python
````
class Node:
    def __init__(self, data):
        self.data = data
        self.next = None

````

C#
````
public class Node {
    public int data;
    public Node next;

    public Node(int data){
        this.data = data;
        this.next = null;
    }
}

````

JavaScript
````
class Node {
    constructor(data)
    {
        this.data = data;
        this.next = null;
    }
}

````', 'A circular linked list is a data structure where the last node connects back to the firstforming a loop This structure allows for continuous traversal withoutany interruptions Circular linked lists are especially helpful fortasks like scheduling and managing playlists this allowing for smooth navigation In this tutorial well cover thebasics of circular linked lists how to work with them their advantagesand disadvantages and their applications', 'Introduction to Circular Linked List', 3, '95a8a2cd-95c1-4ad8-9c8c-61b8dfe820c5', null);
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('583e0208-ddad-4427-a4db-8261b6c5f532', e'
Basic terminologies of Array
----------------------------

* ****Array Index:****
  In an array, elements are identified by their indexes. Array index
  starts from 0.
* ****Array element:**** Elements are items stored in an array and can be accessed by their
  index.
* ****Array Length:****
  The length of an array is determined by the number of elements it can
  contain.

Memory representation of Array
------------------------------

In an array, all the elements are stored in contiguous memory
locations. So, if we initialize an array, the elements will be allocated
sequentially in memory. This allows for efficient access and
manipulation of elements.

![Memory-Representation-of-Array-(1)](https://media.geeksforgeeks.org/wp-content/uploads/20240405101013/Memory-Representation-of-Array-(1).webp)

Declaration of Array
--------------------

Arrays can be declared in various ways in different languages. For
better illustration, below are some language-specific array
declarations:

C++
````
// This array will store integer type element
int arr[5];

// This array will store char type element
char arr[10];

// This array will store float type element
float arr[20];

````

C
````
// This array will store integer type element
int arr[5];

// This array will store char type element
char arr[10];

// This array will store float type element
float arr[20];

````

Java
````
// This array will store integer type element
int arr[];

// This array will store char type element
char arr[];

// This array will store float type element
float arr[];

````

Python
````
# In Python, all types of lists are created same way
arr = []

````

C#
````
// This array will store integer type element
int[] arr;

// This array will store char type element
char[] arr2;

// This array will store float type element
float[] arr3;

````

Javascript
````
// JS code
let arr = []
````


Why do we Need Arrays?
----------------------

Assume there is a class of five students and if we have to keep records
of their marks in examination then, we can do this by declaring five
variables individual and keeping track of records but what if the number
of students becomes very large, it would be challenging to manipulate
and maintain the data.

What it means is that, we can use normal variables (v1, v2, v3, ..)
when we have a small number of objects. But if we want to store a large
number of instances, it becomes difficult to manage them with normal
variables. ****The idea of an array is to represent many instances in one
variable****.


![Importance-of-Array](https://media.geeksforgeeks.org/wp-content/uploads/20240405123859/Importance-of-Array.webp)

Types of Arrays
---------------

Arrays can be classified in two ways:

* On the basis of Size
* On the basis of Dimensions

![Types-of-Arrays](https://media.geeksforgeeks.org/wp-content/uploads/20240731124259/Types-of-Arrays.webp)
### Types of Arrays on the basis of Size:

****1. Fixed Sized Arrays:****

We cannot alter or update the size of this array. Here only a fixed
size (i,e. the size that is mentioned in square brackets ****[]****) of memory will be allocated for storage. In case, we don’t know the
size of the array then if we declare a larger size and store a lesser
number of elements will result in a wastage of memory or we declare a
lesser size than the number of elements then we won’t get enough memory
to store all the elements. In such cases, static memory allocation is
not preferred.

### Types of Arrays on the basis of Dimensions:

****1. One-dimensional Array(1-D Array):**** You can imagine a 1d array as a row, where elements are stored one
after another.

![One-Dimensional-Array(1-D-Array)](https://media.geeksforgeeks.org/wp-content/uploads/20240405123929/One-Dimensional-Array(1-D-Array).webp)

****2. Multi-dimensional Array:****
A multi-dimensional array is an array with more than one dimension. We
can use multidimensional array to store complex data in the form of
tables, etc. We can have 2-D arrays, 3-D arrays, 4-D arrays and so on.


* [****Two-Dimensional Array(2-D Array or Matrix):****](https://www.geeksforgeeks.org/matrix)2-D Multidimensional arrays can be considered as an array of
  arrays or as a matrix consisting of rows and columns.

![Two-Dimensional-Array(2-D-Array-or-Matrix)](https://media.geeksforgeeks.org/wp-content/uploads/20240408165401/Two-Dimensional-Array(2-D-Array-or-Matrix).webp)

* ****Three-Dimensional Array(3-D Array):**** A 3-D Multidimensional array contains three dimensions, so
  it can be considered an array of two-dimensional arrays.

![Three-Dimensional-Array(3-D-Array)](https://media.geeksforgeeks.org/wp-content/uploads/20240408165421/Three-Dimensional-Array(3-D-Array).webp)

Operations on Array
-------------------

### 1. Array Traversal:

Array traversal involves visiting all the elements of the array once.
Below is the implementation of Array traversal in different
Languages:

C++14
````
int arr[] = { 1, 2, 3, 4, 5 };
int len = sizeof(arr) / sizeof(arr[0]);
// Traversing over arr[]
for (int i = 0; i < len; i++) {
    cout << arr[i] << " ";

````

C
````
int arr[] = { 1, 2, 3, 4, 5 };
int len = sizeof(arr) / sizeof(arr[0]);
// Traversing over arr[]
for (int i = 0; i < len; i++) {
    printf("%d ", arr[i]);
}

````
', 'Array is a collection of items of the same variable type that are stored atcontiguous memory locations It is one of the most popular and simpledata structures used in programming', 'Getting Started with Array Data Structure', 2, 'b15d3666-a77b-4098-89f1-225327c74f67', null);
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('10d5cf1e-2b73-47dd-a3f2-ece3832cc16d', e'Let’s take a look a simple example to demonstrate the use of vector
container:


C++
````
#include <bits/stdc++.h>
using namespace std;

int main() {

      // Creating a vector of 5 elements
      vector<int> v = {1, 4, 2, 3, 5};

      for (int i = 0; i < v.size(); i++) {
        cout << v[i] << " ";
    }
      return 0;
}

````






13







1

```
#include <bits/stdc++.h>
```

2

```
using namespace std;
```

3

```
​
```

4

```
int main() {
```

5

```
​
```

6

```
      // Creating a vector of 5 elements
```

7

```
      vector<int> v = {1, 4, 2, 3, 5};
```

8

```
​
```

9

```
      for (int i = 0; i < v.size(); i++) {
```

10

```
        cout << v[i] << " ";
```

11

```
    }
```

12

```
      return 0;
```

13

```
}
```
**Output**
```
1 4 2 3 5
```

Table of Content

* [Syntax of Vector](#create-a-vector)
* [Declaration and Initialization](#initialize-a-vector)
* [Basic Vector Operations](#basic-vector-operations)

+ [Accessing Elements](#accessing-elements)
+ [Updating Elements](#updating-elements)
+ [Traversing Vector](#traversal)
+ [Inserting Elements](#insert)
+ [Deleting Elements](#delete)

* [Other Common Operations on Vector](#common-examples-of-vector-in-c)
* [Passing Vector to Functions](#passing-vector-to-functions)
* [Internal Working of Vector](#internal-working-of-vector)
* [2D Vectors](#2d-vectors)
* [All Member Functions of Vector](#all-member-functions-of-stdvector)

Syntax of Vector
----------------

Vector is defined as the ****std::vector****
class template which contains its implementation and some useful member
functions. It is defined inside the ****<vector>**** header file.

> ****vector****<**T**> vec\\_name;

where,

* ****T:**** Type of elements in the vector.
* ****vec\\_name:**** Name assigned to the vector.

To master vectors and other STL components, check out our [****Complete C++ Course****](https://gfgcdn.com/tu/T5Y/), which covers the ins and outs of C++ STL with real-world examples and
hands-on projects.


Declaration and Initialization
------------------------------

Declaration and initialization are the process of creating an instance
of std::vector class and assigning it some initial value. In C++,
vectors can be declared and initialized in multiple ways as shown
below:

****1. Default Initialization****

An empty vector can be created using the below declaration. This vector
can be filled later on in the program.

> ****vector****<T> vec\\_name;

****2. Initialization with Size and Default Value****

A vector of a specific size can also be declared and initialized to the
given value as default value.

> ****vector****<T> vec\\_name(size, value);

****3. Initialization Using Initializer List****

Vector can also be initialized using a list of values enclosed in ****{} braces**** separated by comma.

> ****vector****<T> vec\\_name = { v1, v2, v3….};
> ****vector****<T> vec\\_name ({ v1, v2, v3….});

Let’s take a look at an example that shows implements the above
methods:

C++
````
#include <bits/stdc++.h>
using namespace std;

void printV(vector<int> &v) {
    for (int i = 0; i < v.size(); i++) {
        cout << v[i] << " ";
    }
    cout << endl;
}

int main() {

    // Creating an empty vector
    vector<int> v1;

    // Creating a vector of 5 elements from
    // initializer list
    vector<int> v2 = {1, 4, 2, 3, 5};

    // Creating a vector of 5 elements with
    // default value
    vector<int> v3(5, 9);

    printV(v1);
    printV(v2);
    printV(v3);

    return 0;
}

````
To know more about accessing vector elements, refer to the article – [How to Access an Element in a Vector in C++?](https://www.geeksforgeeks.org/how-to-access-element-in-vector-using-index-in-cpp/)

### 2. Updating Elements

Updating elements is very similar to the accessing except that we use
an additional assignment operator to assign a new value to a particular
element. It uses the same methods: [] subscript operator and vector
at().

The below example illustrates how to update vector elements:

C++
````

**Output**
```

a c f d z
a c f d z
```

More ways to traverse vectors are discussed in this article – [How to Iterate Through a Vector in C++?](https://www.geeksforgeeks.org/how-to-iterate-through-a-vector-in-cpp/)

### 4. Inserting Elements

An element can be inserted into a vector using [****vector insert()****](https://www.geeksforgeeks.org/vector-insert-function-in-cpp-stl)
method which takes linear time. But for the insertion at the end, the [****vector push\\_back()****](https://www.geeksforgeeks.org/vectorpush_back-vectorpop_back-c-stl/)
method can be used. It is much faster, taking only constant time.

The below example illustrates how to insert elements in the
vector:

C++
````
#include <bits/stdc++.h>
using namespace std;


````

**Output**
```
a c f d z
```

More ways to insert an element in the vector are discussed in this
article – [How to Add Elements in a Vector in C++?](https://www.geeksforgeeks.org/how-to-add-elements-in-a-vector-in-cpp/)

### 5. Deleting Elements

An element can be deleted from a vector using [****vector erase()****](https://www.geeksforgeeks.org/vector-erase-and-clear-in-cpp/)
but this method needs iterator to the element to be deleted. If only the
value of the element is known, then find() function is used to find the
position of this element.

For the deletion at the end, the [****vector pop\\_back()****](https://www.geeksforgeeks.org/vectorpush_back-vectorpop_back-c-stl/)
method can be used, and it is much faster, taking only constant
time.

The below example demonstrates how to delete an element from the
vector:

**Output**
```

a c f d
a c d
```

To know more about the deletion of an element in the vector, refer to
this article – [How to Remove an Element from Vector in C++?](https://www.geeksforgeeks.org/how-to-remove-an-element-from-vector-in-cpp/)

Other Common Operations on Vector
---------------------------------

Vector is one of the most frequently used containers in C++. It is used
in many situations for different purposes. The following examples aim to
help you master vector operations beyond the basics.

> * [Check if a Vector is Empty](https://www.geeksforgeeks.org/how-to-check-if-vector-is-empty-in-cpp/)
> * [Find the Size of a Vector](https://www.geeksforgeeks.org/how-to-find-size-of-vector-in-cpp/)
> * [Resize a Vector](https://www.geeksforgeeks.org/vector-resize-c-stl/)
> * [Reverse a Vector](https://www.geeksforgeeks.org/how-to-reverse-a-vector-using-stl-in-c/)
> * [Sort a Vector in Ascending Order](https://www.geeksforgeeks.org/sort-vector-in-ascending-order-in-cpp/)
> * [Sort a Vector in Descending Order](https://www.geeksforgeeks.org/how-to-sort-a-vector-in-descending-order-using-stl-in-c/)
> * [Different Ways to Copy a Vector](https://www.geeksforgeeks.org/ways-copy-vector-c/)
> * [Swap Two Vectors](https://www.geeksforgeeks.org/vector-swap-in-cpp-stl/)
> * [Remove Duplicates from a Vector](https://www.geeksforgeeks.org/remove-duplicates-from-vector-in-cpp/)

Passing Vector to Functions
---------------------------

Vectors can be passed to a function as arguments just like any other
variable in C++. But it is recommended to pass the vector by reference
so as to avoid the copying of all elements which can be expensive if the
vector is large. Refer to this article to know more – [****Passing Vector to a Function****](https://www.geeksforgeeks.org/passing-vector-function-cpp/)

Internal Working of Vector
--------------------------

Vector internal working is very interesting and useful to select and
optimize its usage. Understanding the internal memory management also
helps in modifying the default mechanism of vector to suits our needs.
Refer to this article to know more – [****Internal Working of Vector****](https://www.geeksforgeeks.org/how-does-a-vector-work-in-c/)

Multidimensional Vectors in C++
-------------------------------

Just like arrays, we can also create multidimensional vectors in C++.
Each element of multidimensional vector can be visualized as the
collection of vectors with dimension one less that the current vector.
For example, 2D vectors are the collection of 1D vectors, while 3D
vectors are the collection of 2D vectors and so on.

With the addition of each dimension, the complexity of operations on
the vectors also increases.

Refer to this article to know more – ****Multidimensional Vectors in C++****

****All Member Functions of Vector****
--------------------------------------

Following is the list of all member functions of std::vector class in
C++:

| Vector Function | Description |
| --- | --- |
| [push\\_back()](https://www.geeksforgeeks.org/vectorpush_back-vectorpop_back-c-stl/) | Adds an element to the end of the vector. |
| [pop\\_back()](https://www.geeksforgeeks.org/vectorpush_back-vectorpop_back-c-stl/) | Removes the last element of the vector. |
| [size()](https://www.geeksforgeeks.org/vectorempty-vectorsize-c-stl/) | Returns the number of elements in the vector. |
| [max\\_size()](https://www.geeksforgeeks.org/vector-max_size-function-in-c-stl/) | Returns the maximum number of elements that the vector can hold. |
| [resize()](https://www.geeksforgeeks.org/vector-resize-c-stl/) | Changes the size of the vector. |
| [empty()](https://www.geeksforgeeks.org/vectorempty-vectorsize-c-stl/) | Checks if the vector is empty. |
| [operator[]](https://www.geeksforgeeks.org/vectoroperator-vectoroperator-c-stl/) | Accesses the element at a specific position. |
| [at()](https://www.geeksforgeeks.org/vectorat-vectorswap-c-stl/) | Accesses the element at a specific position, with bounds checking. |
| [front()](https://www.geeksforgeeks.org/vectorfront-vectorback-c-stl/) | Accesses the first element of the vector. |
| [back()](https://www.geeksforgeeks.org/vectorfront-vectorback-c-stl/) | Accesses the last element of the vector. |
| [begin()](https://www.geeksforgeeks.org/vectorbegin-vectorend-c-stl/) | Returns an iterator pointing to the first element of the vector. |
| [end()](https://www.geeksforgeeks.org/vectorbegin-vectorend-c-stl/) | Returns an iterator pointing to the past-the-end element of the vector. |
| [rbegin()](https://www.geeksforgeeks.org/vector-rbegin-and-rend-function-in-c-stl/) | Returns a reverse iterator pointing to the last element of the vector. |
| [rend()](https://www.geeksforgeeks.org/vector-rbegin-and-rend-function-in-c-stl/) | Returns a reverse iterator pointing to the element preceding the first element of the vector. |
| [cbegin](https://www.geeksforgeeks.org/vector-cbegin-vector-cend-c-stl/) | Returns const\\_iterator to beginning |
| [cend](https://www.geeksforgeeks.org/vector-cbegin-vector-cend-c-stl/) | Returns const\\_iterator to end |
| [crbegin](https://www.geeksforgeeks.org/vectorcrend-vectorcrbegin-examples/) | Returns const\\_reverse\\_iterator to reverse beginning |
| [crend](https://www.geeksforgeeks.org/vectorcrend-vectorcrbegin-examples/) | Returns const\\_reverse\\_iterator to reverse end |
| [insert()](https://www.geeksforgeeks.org/vector-insert-function-in-cpp-stl/) | Inserts elements at a specific position in the vector. |
| [erase()](https://www.geeksforgeeks.org/vector-erase-and-clear-in-cpp/) | Removes elements from a specific position or range in the vector. |
| [swap()](https://www.geeksforgeeks.org/vectorat-vectorswap-c-stl/) | Swaps the contents of the vector with those of another vector. |
| [clear()](https://www.geeksforgeeks.org/vector-erase-and-clear-in-cpp/) | Removes all elements from the vector. |
| [emplace()](https://www.geeksforgeeks.org/vector-emplace-function-in-c-stl/) | Constructs and inserts an element in the vector. |
| [emplace\\_back()](https://www.geeksforgeeks.org/vectoremplace_back-c-stl/) | Constructs and inserts an element at the end of the vector. |
| [assign()](https://www.geeksforgeeks.org/vector-assign-in-c-stl/) | Assigns new values to the vector elements by replacing old ones. |
| [capacity()](https://www.geeksforgeeks.org/vector-capacity-function-in-c-stl/) | Returns the size of the storage space currently allocated to the vector. |
| [reserve()](https://www.geeksforgeeks.org/using-stdvectorreserve-whenever-possible/) | Requests that the vector capacity be at least enough to contain a specified number of elements. |
| [shrink\\_to\\_fit()](https://www.geeksforgeeks.org/vector-shrink_to_fit-function-in-c-stl/) | Reduces memory usage by freeing unused space. |
| [data()](https://www.geeksforgeeks.org/vector-data-function-in-c-stl/) | Returns a direct pointer to the memory array used internally by the vector to store its owned elements. |
| [get\\_allocator](https://www.geeksforgeeks.org/get_allocator-in-cpp/) | Returns a copy of the allocator object associated with the vector. |




Subscribe for 1 Year and get
**1 Extra year of access completely FREE!** Upgrade
to [GeeksforGeeks Premium](https://www.geeksforgeeks.org/geeksforgeeks-premium-subscription?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium)
today!

Choose
[GeeksforGeeks Premium](https://www.geeksforgeeks.org/geeksforgeeks-premium-subscription?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium)
and also get access to
**50+ Courses with Certifications,**
**Unlimited Article Summarization, 100% Ad free environment, A.I. Bot
support** in all coding problems, and much more.
[Go Premium!](https://www.geeksforgeeks.org/geeksforgeeks-premium-subscription?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium)
', 'In C vectoris a dynamic array with the ability to resize itself automatically whenan element is inserted or deleted It is the part Standard TemplateLibrary STL and provide various useful functions for datamanipulation', 'Vector in C++ STL', 3, 'b15d3666-a77b-4098-89f1-225327c74f67', null);
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('c38182ca-7375-4150-9ef3-c922e811388b', e'****FIFO Principle in Queue:****

FIFO Principle states that the first element added to the Queue will be
the first one to be removed or processed. So, Queue is like a line of
people waiting to purchase tickets, where the first person in line is
the first person served. (i.e. First Come First Serve).


![FIFO-Principle-First-In-First-Out-1](https://media.geeksforgeeks.org/wp-content/uploads/20241213121625618949/FIFO-Principle-First-In-First-Out-1.webp)

Basic Terminologies of Queue
----------------------------

* ****Front:**** Position of the entry in a queue ready to be served, that is, the
  first entry that will be removed from the queue, is called the ****front**** of the queue. It is also referred as the ****head**** of the queue.
* ****Rear:****
  Position of the last entry in the queue, that is, the one most
  recently added, is called the ****rear**** of the queue. It is also referred as the ****tail**** of the queue.
* ****Size:**** Size refers to the ****current**** number of elements in the queue.
* ****Capacity:**** Capacity refers to the ****maximum**** number of elements the queue can hold.

****Representation of Queue****
-------------------------------

![Representation-of-Queue-Data-Structure](https://media.geeksforgeeks.org/wp-content/uploads/20241212130245410876/Representation-of-Queue-Data-Structure-768.webp)

Operations on Queue
-------------------

### ****1. Enqueue:****

Enqueue operation ****adds (or stores) an element to the end of the queue****.

****Steps:****

1. Check if the ****queue is full****. If so, return an ****overflow**** error and exit.
2. If the queue is ****not full****, increment the ****rear**** pointer to the next available position.
3. Insert the element at the rear.


![Enqueue-Operation-in-Queue-01.webp](https://media.geeksforgeeks.org/wp-content/uploads/20241212125010297668/Enqueue-Operation-in-Queue-01.webp)![Enqueue-Operation-in-Queue-01.webp](https://media.geeksforgeeks.org/wp-content/uploads/20241212125010297668/Enqueue-Operation-in-Queue-01.webp)


![Enqueue-Operation-in-Queue-02.webp](https://media.geeksforgeeks.org/wp-content/uploads/20241212125010180461/Enqueue-Operation-in-Queue-02.webp)![Enqueue-Operation-in-Queue-02.webp](https://media.geeksforgeeks.org/wp-content/uploads/20241212125010180461/Enqueue-Operation-in-Queue-02.webp)


![Enqueue-Operation-in-Queue-03.webp](https://media.geeksforgeeks.org/wp-content/uploads/20241212125010068824/Enqueue-Operation-in-Queue-03.webp)![Enqueue-Operation-in-Queue-03.webp](https://media.geeksforgeeks.org/wp-content/uploads/20241212125010068824/Enqueue-Operation-in-Queue-03.webp)


![Enqueue-Operation-in-Queue-04.webp](https://media.geeksforgeeks.org/wp-content/uploads/20241212125009956179/Enqueue-Operation-in-Queue-04.webp)![Enqueue-Operation-in-Queue-04.webp](https://media.geeksforgeeks.org/wp-content/uploads/20241212125009956179/Enqueue-Operation-in-Queue-04.webp)


![Enqueue-Operation-in-Queue-05.webp](https://media.geeksforgeeks.org/wp-content/uploads/20241212125009838736/Enqueue-Operation-in-Queue-05.webp)![Enqueue-Operation-in-Queue-05.webp](https://media.geeksforgeeks.org/wp-content/uploads/20241212125009838736/Enqueue-Operation-in-Queue-05.webp)


![Enqueue-Operation-in-Queue-06.webp](https://media.geeksforgeeks.org/wp-content/uploads/20241212125009700162/Enqueue-Operation-in-Queue-06.webp)![Enqueue-Operation-in-Queue-06.webp](https://media.geeksforgeeks.org/wp-content/uploads/20241212125009700162/Enqueue-Operation-in-Queue-06.webp)
', 'Queue is a linear data structure that follows FIFO First In First Out Principle so the first element inserted is the first to be popped out', 'Introduction', 1, '940fddca-cb93-47d1-a28f-355e3c4490d2', '6cff50e2-98ea-4e30-a430-e5462b59d3a1');
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('b43dc623-9b99-4b0e-b9c5-4819fe7af85b', e'It means that the element that is inserted first in the queue will
come out first and the element that is inserted last will come out last.
It is an ordered list in which insertion of an element is done from one
end which is known as the rear end and deletion of an element is done
from the other which is known as the front end. Similar to stacks,
multiple operations can be performed on the queue. When an element is
inserted in a queue, then the operation is known as ****Enqueue**** and when an element is deleted from the queue, then the operation is
known as ****Dequeue.****It is important to know that we cannot insert an element if the size of
the queue is full and cannot delete an element when the queue itself is
empty. If we try to insert an element even after the queue is full, then
such a condition is known as overflow whereas, if we try to delete an
element even after the queue is empty then such a condition is known as
underflow.

****Primary Queue Operations:****

* ****void enqueue(int Element):**** When this operation is performed, an element is inserted in the queue
  at the end i.e. at the rear end. (Where T is Generic i.e we can define
  Queue of any type of data structure.) This operation take ****constant time i.e O(1).****
* ****int dequeue():****
  When this operation is performed, an element is removed from the front
  end and is returned. This operation take ****constant time i.e O(1).****

****Auxiliary Queue Operations:****

* ****int front():**** This operation will return the element at the front without removing
  it and it take O(1) time.
* ****int rear():**** This operation will return the element at the rear without removing
  it, Its Time Complexity is O(1).
* ****int isEmpty():**** This operation indicates whether the queue is empty or not. This
  Operation also done in O(1).
* ****int size():**** This operation will return the size of the queue i.e. the total
  number of elements present in the queue and it’s time complexity is
  O(1).

****Types of Queues:****

* ****Simple Queue:**** Simple queue also known as a linear queue is the most basic version
  of a queue. Here, insertion of an element i.e. the Enqueue operation
  takes place at the rear end and removal of an element i.e. the Dequeue
  operation takes place at the front end.
* ****Circular Queue:****This is mainly an efficient array implementation of Simple Queue. In
  a circular queue, the element of the queue act as a circular ring. The
  working of a circular queue is similar to the linear queue except for
  the fact that the last element is connected to the first element. Its
  advantage is that the memory is utilized in a better way. This is
  because if there is an empty space i.e. if no element is present at a
  certain position in the queue, then an element can be easily added at
  that position.
* ****Priority Queue:**** This queue is a special type of queue. Its specialty is that it
  arranges the elements in a queue based on some priority. The priority
  can be something where the element with the highest value has the
  priority so it creates a queue with decreasing order of values. The
  priority can also be such that the element with the lowest value gets
  the highest priority so in turn it creates a queue with increasing
  order of values.
* ****Dequeue:**** Dequeue is also known as Double Ended Queue. As the name suggests
  double ended, it means that an element can be inserted or removed from
  both the ends of the queue unlike the other queues in which it can be
  done only from one end. Because of this property it may not obey the
  First In First Out property.

****Implementation of Queue:****

* ****Sequential allocation:**** A queue can be implemented using an array. It can organize a limited
  number of elements.
* ****Linked list allocation:****
  A queue can be implemented using a linked list. It can organize an
  unlimited number of elements.

****Applications of Queue:****

* ****Multi programming:**** Multi programming means when multiple programs are running in the
  main memory. It is essential to organize these multiple programs and
  these multiple programs are organized as queues.
* ****Network:**** In a network, a queue is used in devices such as a router or a
  switch. another application of a queue is a mail queue which is a
  directory that stores data and controls files for mail messages.
* ****Job Scheduling:**** The computer has a task to execute a particular number of jobs that
  are scheduled to be executed one after another. These jobs are
  assigned to the processor one by one which is organized using a
  queue.
* ****Shared resources:**** Queues are used as waiting lists for a single shared resource.

****Real-time application of Queue:****

* Working as a buffer between a slow and a fast device. For example
  keyboard and CPU, and two devices on network.
* ATM Booth Line
* Ticket Counter Line
* CPU task scheduling
* Waiting time of each customer at call centers.

****Advantages of Queue:****

* A large amount of data can be managed efficiently with ease.
* Operations such as insertion and deletion can be performed with ease
  as it follows the first in first out rule.
* Queues are useful when a particular service is used by multiple
  consumers.
* Queues are fast in speed for data inter-process communication.
* Queues can be used in the implementation of other data
  structures.

****Disadvantages of Queue:****

* The operations such as insertion and deletion of elements from the
  middle are time consuming.
* In a classical queue, a new element can only be inserted when the
  existing elements are deleted from the queue.
* Searching an element takes O(N) time.
* Maximum size of a queue must be defined prior in case of array
  implementation.

Join
[GfG 160](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium), a 160-day journey of coding challenges aimed at sharpening your
skills. Each day, solve a handpicked problem, dive into detailed
solutions through articles and videos, and enhance your preparation for
any interview—all for free! Plus, win exciting GfG goodies along the
way! -
[Explore Now](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium)', 'A Queue is a linear data structure This data structure follows a particularorder in which the operations are performed The order is First In First Out', 'Applications, Advantages and Disadvantages of Queue', 4, '940fddca-cb93-47d1-a28f-355e3c4490d2', null);
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('164baabf-24c9-45ec-9abf-02b56095c0e8', e'**Types of Queues:**

There are **five different types of queues** that are used in
different scenarios. They are:

1. Input Restricted Queue (this is a Simple Queue)
2. Output Restricted Queue (this is also a Simple Queue)
3. Circular Queue
4. Double Ended Queue (Deque)
5. Priority Queue
   * Ascending Priority Queue
   * Descending Priority Queue

![Types of Queues](https://media.geeksforgeeks.org/wp-content/uploads/20220623134709/typesofqueues.jpg)

Types of Queues

**1.** [**Circular Queue**](https://www.geeksforgeeks.org/circular-queue-set-1-introduction-array-implementation/)**:** Circular Queue is a linear data structure in which the
operations are performed based on FIFO (First In First Out) principle and
the last position is connected back to the first position to make a
circle. It is also called **‘Ring Buffer’**. This queue is
primarily used in the following cases:

1. **Memory Management:** The unused memory locations in the
   case of ordinary queues can be utilized in circular queues.
2. **Traffic system:** In a computer-controlled traffic
   system, circular queues are used to switch on the traffic lights one by
   one repeatedly as per the time set.
3. **CPU Scheduling:** Operating systems often maintain a
   queue of processes that are ready to execute or that are waiting for a
   particular event to occur.

The time complexity for the circular Queue is O(1).

**2. Input restricted Queue:** In this type of Queue, the
input can be taken from one side only(rear) and deletion of elements can
be done from both sides(front and rear). This kind of Queue does not
follow FIFO(first in first out).  This queue is used in cases where
the consumption of the data needs to be in FIFO order but if there is a
need to remove the recently inserted data for some reason and one such
case can be irrelevant data, performance issue, etc.



![Input Restricted Queue](https://media.geeksforgeeks.org/wp-content/uploads/20220623131417/inputrestrictedqueue.jpg)

Input Restricted Queue

**Advantages of Input restricted Queue:**

* Prevents overflow and overloading of the queue by limiting the number of
  items added
* Helps maintain stability and predictable performance of the system

**Disadvantages of Input restricted Queue:**

* May lead to resource wastage if the restriction is set too low and items
  are frequently discarded
* May lead to waiting or blocking if the restriction is set too high and
  the queue is full, preventing new items from being added.

**3. Output restricted Queue:** In this type of Queue, the
input can be taken from both sides(rear and front) and the deletion of the
element can be done from only one side(front).  This queue is used in
the case where the inputs have some priority order to be executed and the
input can be placed even in the first place so that it is executed
first.

![Output Restricted Queue](https://media.geeksforgeeks.org/wp-content/uploads/20220623131455/outputrestrictedqueue.jpg)

Output Restricted Queue

**4.** [**Double ended Queue**](https://www.geeksforgeeks.org/deque-set-1-introduction-applications/)**:** Double Ended Queue is also a Queue data structure in
which the insertion and deletion operations are performed at both the ends
(front and rear). That means, we can insert at both front and rear
positions and can delete from both front and rear positions.  Since
Deque supports both stack and queue operations, it can be used as both.
The Deque data structure supports clockwise and anticlockwise rotations in
O(1) time which can be useful in certain applications. Also, the problems
where elements need to be removed and or added both ends can be
efficiently solved using Deque.

![Double Ended Queue](https://media.geeksforgeeks.org/wp-content/uploads/20220623131811/doubleended.jpg)

Double Ended Queue

**5.** [**Priority Queue**](https://www.geeksforgeeks.org/priority-queue-set-1-introduction/)**:** A priority queue is a special type of queue in which
each element is associated with a priority and is served according to its
priority. There are two types of Priority Queues. They are:

1. **Ascending Priority Queue:** Element can be inserted
   arbitrarily but only smallest element can be removed. For example,
   suppose there is an array having elements 4, 2, 8 in the same order. So,
   while inserting the elements, the insertion will be in the same sequence
   but while deleting, the order will be 2, 4, 8.
2. **Descending priority Queue:** Element can be inserted
   arbitrarily but only the largest element can be removed first from the
   given Queue. For example, suppose there is an array having elements 4,
   2, 8 in the same order. So, while inserting the elements, the insertion
   will be in the same sequence but while deleting, the order will be 8, 4,
   2.

The time complexity of the Priority Queue is O(logn).

[**Applications of a Queue:**](https://www.geeksforgeeks.org/applications-of-queue-data-structure/)

The
[queue](https://www.geeksforgeeks.org/queue-set-1introduction-and-array-implementation/)
is used when things don’t have to be processed immediately, but have to be
processed in First In First Out order like
[Breadth First Search](https://www.geeksforgeeks.org/breadth-first-search-or-bfs-for-a-graph/). This property of Queue makes it also useful in the following kind of
scenarios.

1. When a resource is shared among multiple consumers. Examples include
   [CPU scheduling](https://www.geeksforgeeks.org/cpu-scheduling-in-operating-systems/),
   [Disk Scheduling](https://www.geeksforgeeks.org/disk-scheduling-algorithms/).
2. When data is transferred asynchronously (data not necessarily received
   at the same rate as sent) between two processes. Examples include IO
   Buffers,
   [pipes](https://www.geeksforgeeks.org/piping-in-unix-or-linux/), file IO, etc.
3. Linear Queue: A linear queue is a type of queue where data elements are
   added to the end of the queue and removed from the front of the queue.
   Linear queues are used in applications where data elements need to be
   processed in the order in which they are received. Examples include
   printer queues and message queues.
4. Circular Queue: A circular queue is similar to a linear queue, but the
   end of the queue is connected to the front of the queue. This allows for
   efficient use of space in memory and can improve performance. Circular
   queues are used in applications where the data elements need to be
   processed in a circular fashion. Examples include CPU scheduling and
   memory management.
5. Priority Queue: A priority queue is a type of queue where each element
   is assigned a priority level. Elements with higher priority levels are
   processed before elements with lower priority levels. Priority queues
   are used in applications where certain tasks or data elements need to be
   processed with higher priority. Examples include operating system task
   scheduling and network packet scheduling.
6. Double-ended Queue: A double-ended queue, also known as a deque, is a
   type of queue where elements can be added or removed from either end of
   the queue. This allows for more flexibility in data processing and can
   be used in applications where elements need to be processed in multiple
   directions. Examples include job scheduling and searching algorithms.
7. Concurrent Queue: A concurrent queue is a type of queue that is designed
   to handle multiple threads accessing the queue simultaneously.
   Concurrent queues are used in multi-threaded applications where data
   needs to be shared between threads in a thread-safe manner. Examples
   include database transactions and web server requests.

**Issues of Queue :**

Some common issues that can arise when using queues:

1. Queue overflow: Queue overflow occurs when the queue reaches its maximum
   capacity and is unable to accept any more elements. This can cause data
   loss and can lead to application crashes.
2. Queue underflow: Queue underflow occurs when an attempt is made to
   remove an element from an empty queue. This can cause errors and
   application crashes.
3. Priority inversion: Priority inversion occurs in priority queues when a
   low-priority task holds a resource that a high-priority task needs. This
   can cause delays in processing and can impact system performance.
4. Deadlocks: Deadlocks occur when multiple threads or processes are
   waiting for each other to release resources, resulting in a situation
   where none of the threads can proceed. This can happen when using
   concurrent queues and can lead to system crashes.
5. Performance issues: Queue performance can be impacted by various
   factors, such as the size of the queue, the frequency of access, and the
   type of operations performed on the queue. Poor queue performance can
   lead to slower system performance and reduced user experience.
6. Synchronization issues: Synchronization issues can arise when multiple
   threads are accessing the same queue simultaneously. This can result in
   data corruption, race conditions, and other errors.
7. Memory management issues: Queues can use up significant amounts of
   memory, especially when processing large data sets. Memory leaks and
   other memory management issues can occur, leading to system crashes and
   other errors.

**Reference :**

Some references for further reading on queues:

1. “Data Structures and Algorithms in Java” by Robert Lafore – This book
   provides an in-depth explanation of different types of queues and their
   implementations in Java.
2. “Introduction to Algorithms” by Thomas H. Cormen et al. – This textbook
   covers the basic concepts of data structures and algorithms, including
   queues and their various applications.
3. “Concurrency in C# Cookbook” by Stephen Cleary – This book provides
   practical examples of how to use concurrent queues in C# programming.
4. “Queue (abstract data type)” on Wikipedia – This article provides an
   overview of queues and their properties, as well as examples of their
   applications.
5. “The Art of Computer Programming, Volume 1: Fundamental Algorithms” by
   Donald E. Knuth – This book includes a detailed analysis of different
   queue algorithms and their performance.
6. “Queues and the Producer-Consumer Problem” by Douglas C. Schmidt – This
   paper discusses how queues can be used to solve the producer-consumer
   problem in concurrent programming.', 'Introduction A Queue is a linear structure that follows a particular order in which theoperations are performed The order is First In First Out FIFO A goodexample of a queue is any queue of consumers for a resource where theconsumer that came first is served first In this article the differenttypes of queues are discussed', 'Different Types of Queues and its Applications', 3, '940fddca-cb93-47d1-a28f-355e3c4490d2', null);
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('26a3b187-53b3-4f94-85ee-dfc014ad80e9', e'Understanding Node Structure
----------------------------

In a singly linked list, each node consists of two parts: data and a
pointer to the next node. This structure allows nodes to be dynamically
linked together, forming a chain-like sequence.


![Singly-Linked-List](https://media.geeksforgeeks.org/wp-content/uploads/20240917161540/Singly-Linked-List.webp)

Singly Linked List



C++
````
// Definition of a Node in a singly linked list
struct Node {

    // Data part of the node
    int data;

    // Pointer to the next node in the list
    Node* next;

    // Constructor to initialize the node with data
    Node(int data)
    {
        this->data = data;
        this->next = nullptr;
    }
};

````

C
````
// Definition of a Node in a singly linked list
struct Node {
    int data;
    struct Node* next;
};

// Function to create a new Node
struct Node* newNode(int data) {
    struct Node* temp =
      (struct Node*)malloc(sizeof(struct Node));
    temp->data = data;
    temp->next = NULL;
    return temp;
}

````

Java
````
// Definition of a Node in a singly linked list
public class Node {
    int data;
    Node next;

    // Constructor to initialize the node with data
    public Node(int data)
    {
        this.data = data;
        this.next = null;
    }
}

````

Python
````
# Definition of a Node in a singly linked list
class Node:
    def __init__(self, data):
       # Data part of the node
        self.data = data
        self.next = None

````

JavaScript
````
// Definition of a Node in a singly linked list
class Node {
    constructor(data) {
    // Data part of the node
        this.data = data;
        this.next = null;
    }
}

````', 'A singly linked list is a fundamental data structure it consists of nodes where each node contains a data field and a reference to the next node in the linked list The next of the last node is null indicating the end of the list Linked Lists support efficientinsertion and deletion operations', 'Singly Linked List', 1, '95a8a2cd-95c1-4ad8-9c8c-61b8dfe820c5', '72099d95-a4ec-4d5c-bf8c-05b8b022aaaa');
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('507be1b8-9a50-4b8c-b04a-399b55be3595', e'What is Array in C?
-------------------

An array in C is a fixed-size collection of similar data items stored
in contiguous memory locations. It can be used to store the collection
of primitive data types such as int, char, float, etc., and also derived
and user-defined data types such as pointers, structures, etc.


![arrays in c](https://media.geeksforgeeks.org/wp-content/uploads/20230302091959/Arrays-in-C.png)

C Array Declaration
-------------------

In C, we have to declare the array like any other variable before using
it. We can declare an array by specifying its name, the type of its
elements, and the size of its dimensions. When we declare an array in C,
the compiler allocates the memory block of the specified size to the
array name. To deepen your understanding of arrays and their role in
building complex data structures, the[****C Programming Course Online with Data Structures****](https://gfgcdn.com/tu/T3E/)
offers a comprehensive guide to arrays and their practical applications
in C.

### Syntax of Array Declaration

```
data_type array_name [size];
         or
data_type array_name [size1] [size2]...[sizeN];
```

where N is the number of dimensions.

![c array delcaration](https://media.geeksforgeeks.org/wp-content/uploads/20230302092603/c-array-declaration.png)

The C arrays are static in nature, i.e., they are allocated memory at
the compile time.

### Example of Array Declaration

C
````
// C Program to illustrate the array declaration
#include <stdio.h>

int main()
{

    // declaring array of integers
    int arr_int[5];
    // declaring array of characters
    char arr_char[5];

    return 0;
}

````
**Output**

C Array Initialization
----------------------

Initialization in C is the process to assign some initial value to the
variable. When the array is declared or allocated memory, the elements
of the array contain some garbage value. So, we need to initialize the
array to some meaningful value. There are multiple ways in which we can
initialize an array in C.

### 1. Array Initialization with Declaration

In this method, we initialize the array along with its declaration. We
use an initializer list to initialize multiple elements of the array. An
initializer list is the list of values enclosed within braces ****{ }**** separated b a comma.


```
data_type array_name [size] = {value1, value2, ... valueN};
```
![c array initialization](https://media.geeksforgeeks.org/wp-content/uploads/20230302092653/C-array-initialization.png)
### 2. Array Initialization with Declaration without Size

If we initialize an array using an initializer list, we can skip
declaring the size of the array as the compiler can automatically deduce
the size of the array in these cases. The size of the array in these
cases is equal to the number of elements present in the initializer list
as the compiler can automatically deduce the size of the array.

```
data_type array_name[] = {1,2,3,4,5};
```

The size of the above arrays is 5 which is automatically deduced by the
compiler.

### 3. Array Initialization after Declaration (Using Loops)

We initialize the array after the declaration by assigning the initial
value to each element individually. We can use for loop, while loop, or
do-while loop to assign the value to each element of the array.

```
for (int i = 0; i < N; i++) {
    array_name[i] = valuei;
}
```
### Example of Array Initialization in C

C
````
// C Program to demonstrate array initialization
#include <stdio.h>

int main()
{

    // array initialization using initialier list
    int arr[5] = { 10, 20, 30, 40, 50 };

    // array initialization using initializer list without
    // specifying size
    int arr1[] = { 1, 2, 3, 4, 5 };

    // array initialization using for loop
    float arr2[5];
    for (int i = 0; i < 5; i++) {
        arr2[i] = (float)i * 2.1;
    }
    return 0;
}

````

**Output**

Access Array Elements
---------------------

We can access any element of an array in C using the array subscript
operator ****[ ]**** and the index value*****i***** of the element.

```
array_name [index];
```

One thing to note is that the indexing in the array always starts with
0, i.e., the ****first element**** is at index ****0**** and the ****last element**** is at ****N – 1**** where ****N**** is the number of elements in the array.

![access array elements](https://media.geeksforgeeks.org/wp-content/uploads/20230302092738/access-array-elements.png)
### Example of Accessing  Array Elements using Array Subscript Operator

C
````
// C Program to illustrate element access using array
// subscript
#include <stdio.h>

int main()
{

    // array declaration and initialization
    int arr[5] = { 15, 25, 35, 45, 55 };

    // accessing element at index 2 i.e 3rd element
    printf("Element at arr[2]: %d\\n", arr[2]);

    // accessing element at index 4 i.e last element
    printf("Element at arr[4]: %d\\n", arr[4]);

    // accessing element at index 0 i.e first element
    printf("Element at arr[0]: %d", arr[0]);

    return 0;
}

````
', 'Array in C is one of the most used data structures in C programming It is asimple and fast way of storing multiple values under a single name Inthis article we will study the different aspects of array in C languagesuch as array declaration definition initialization types of arraysarray syntax advantages and disadvantages and many more', 'C Arrays', 1, 'b15d3666-a77b-4098-89f1-225327c74f67', '78ed7e4e-8f14-4994-9d33-42f1a1199bf8');
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('31e682f1-4ab5-4959-866b-69023002d59f', e'Advantages of Linked Lists (or Most Common Use Cases):
------------------------------------------------------

* Linked Lists are mostly used because of their effective insertion and
  deletion.  We only need to change few pointers (or references) to
  insert (or delete) an item in the middle
* [Insertion and deletion](https://www.geeksforgeeks.org/insertion-deletion-stl-set-c/) at any point in a linked list take O(1) time. Whereas in an [array](https://www.geeksforgeeks.org/array-data-structure/)
  data structure, insertion / deletion in the middle takes O(n)
  time.
* This data structure is simple and can be also used to implement [a stack](https://www.geeksforgeeks.org/stack-data-structure/), [queues,](https://www.geeksforgeeks.org/queue-data-structure/) and other [abstract data structures](https://www.geeksforgeeks.org/abstract-data-types/).
* Implementation of Queue and Deque data structures : Simple array
  implementation is not efficient at all. We must use circular array to
  efficiently implement which is complex. But with linked list, it is
  easy and straightforward. That is why most of the language libraries
  use Linked List internally to implement these data structures..
* Linked List might turn out to be more space efficient compare to
  arrays in cases where we cannot guess the number of elements in
  advance. In case of arrays, the whole memory for items is allocated
  together. Even with dynamic sized arrays like vector in C++ or list in
  Python or ArrayList in Java. the internal working involves
  de-allocation of whole memory and allocation of a bigger chunk when
  insertions happen beyond the current capacity.

Applications of Linked Lists:
-----------------------------

* Linked Lists can be used to implement stacks, queue, deque, [sparse matrices](https://www.geeksforgeeks.org/sparse-matrix-representation/) and adjacency list representation of graphs.
* [Dynamic memory allocation](https://www.geeksforgeeks.org/what-is-dynamic-memory-allocation/)
  in operating systems and compilers (linked list of free blocks).
* Manipulation of polynomials
* Arithmetic operations on long integers.
* In operating systems, they can be used in Memory management, process
  scheduling (for example circular linked list for round robin
  scheduling) and file system.
* Algorithms that need to frequently insert or delete items from large
  collections of data.
* LRU cache, which uses a doubly linked list to keep track of the most
  recently used items in a cache.

Applications of Linked Lists in real world:
-------------------------------------------

* The list of songs in the music player are linked to the previous and
  next songs.
* In a web browser, previous and next web page URLs can be linked
  through the previous and next buttons (Doubly Linked List)
* In image viewer, the previous and next images can be linked with the
  help of the previous and next buttons (Doubly Linked List)
* Circular Linked Lists can be used to implement things in round manner
  where we go to every element one by one.
* Linked List are preferred over arrays for implementations of Queue
  and Deque data structures because of fast deletions (or insertions)
  from the front of the linked lists.

Disadvantages of Linked Lists:
------------------------------

Linked lists are a popular data structure in computer science, but like
any other data structure, they have certain disadvantages as well. Some
of the key disadvantages of linked lists are:

* ****Slow Access Time:**** Accessing elements in a linked list can be slow, as you need to
  traverse the linked list to find the element you are looking for,
  which is an O(n) operation. This makes linked lists a poor choice for
  situations where you need to access elements quickly.
* ****Pointers or References:****
  Linked lists use pointers or references to access the next node, which
  can make them more complex to understand and use compared to arrays.
  This complexity can make linked lists more difficult to debug and
  maintain.
* ****Higher overhead:**** Linked lists have a higher overhead compared to arrays, as each node
  in a linked list requires extra memory to store the reference to the
  next node.
* ****Cache Inefficiency:**** Linked lists are cache-inefficient because the memory is not
  contiguous. This means that when you traverse a linked list, you are
  not likely to get the data you need in the cache, leading to cache
  misses and slow performance.

In conclusion, linked lists are a powerful and flexible data structure,
but they have certain disadvantages that need to be taken into
consideration when deciding whether to use them or not. For example, if
you need fast access time, arrays might be a better choice, but if you
need to insert or delete elements frequently, linked lists might be the
better choice.

Join
[GfG 160](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium), a 160-day journey of coding challenges aimed at sharpening your
skills. Each day, solve a handpicked problem, dive into detailed
solutions through articles and videos, and enhance your preparation for
any interview—all for free! Plus, win exciting GfG goodies along the
way! -
[Explore Now](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium)', 'A Linked List is a linear data structure that is used to store a collection of data with the help of nodesPlease remember the following points before moving forward The consecutive elements are connected by pointers   references The last node of the linked list points to null The entry point of a linked list is known as the head The common variations of linked lists are Singly Doubly Singly Circular and Doubly Circular', 'Applications, Advantages and Disadvantages of Linked List', 4, '95a8a2cd-95c1-4ad8-9c8c-61b8dfe820c5', null);
INSERT INTO public.lessons (lesson_id, content, description, lesson_name, lesson_order, course_id, exercise_id) VALUES ('275bb4fa-f53f-430e-b85c-dc60af485489', e'Table of Content

* [Applications of Array Data Structure:](#applications-of-array-data-structure)
* [Advantages of Array Data Structure:](#advantages-of-array-data-structure)
* [Disadvantages of Array Data Structure:](#disadvantages-of-array-data-structure)
### ****Applications of Array Data Structure:****

Arrays mainly have advantages like random access and cache friendliness
over other data structures that make them useful.

Below are some applications of arrays.

* ****Storing and accessing data****: Arrays store elements in a specific order and allow constant-time
  O(1) access to any element.
* ****Searching****: If data in array is sorted, we can search an item in O(log n) time.
  We can also find floor(), ceiling(), kth smallest, kth largest, etc
  efficiently.
* ****Matrices****: Two-dimensional arrays are used for matrices in computations like
  graph algorithms and image processing.
* ****Implementing other data structures:****
  Arrays are used as the underlying data structure for implementing
  stacks and queues.
* ****Dynamic programming****: Dynamic programming algorithms often use arrays to store
  intermediate results of subproblems in order to solve a larger
  problem.
* ****Data Buffers:****
  Arrays serve as data buffers and queues, temporarily storing incoming
  data like network packets, file streams, and database results before
  processing.

### ****Advantages of Array Data Structure:****

* ****Efficient and Fast Access:****
  Arrays allow direct and efficient access to any element in the
  collection with constant access time, as the data is stored in
  contiguous memory locations.
* ****Memory Efficiency:****
  Arrays store elements in contiguous memory, allowing efficient
  allocation in a single block and reducing memory fragmentation.
* ****Versatility:****
  Arrays can be used to store a wide range of data types, including
  integers, floating-point numbers, characters, and even complex data
  structures such as objects and pointers.
* ****Compatibility with hardware:**** The array data structure is compatible with most hardware
  architectures, making it a versatile tool for programming in a wide
  range of environments.

### ****Disadvantages of Array Data Structure:****

* ****Fixed Size:****
  Arrays have a fixed size set at creation. Expanding an array requires
  creating a new one and copying elements, which is time-consuming and
  memory-intensive.
* ****Memory Allocation Issues:****
  Allocating large arrays can cause memory exhaustion, leading to
  crashes, especially on systems with limited resources.
* ****Insertion and Deletion Challenges:****
  Adding or removing elements requires shifting subsequent elements,
  making these operations inefficient.
* ****Limited Data Type Support:****
  Arrays support only elements of the same type, limiting their use with
  complex data types.
* ****Lack of Flexibility:****
  Fixed size and limited type support make arrays less adaptable than
  structures like linked lists or trees.

Join
[GfG 160](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium), a 160-day journey of coding challenges aimed at sharpening your
skills. Each day, solve a handpicked problem, dive into detailed
solutions through articles and videos, and enhance your preparation for
any interview—all for free! Plus, win exciting GfG goodies along the
way! -
[Explore Now](https://www.geeksforgeeks.org/courses/gfg-160-series?utm_source=geeksforgeeks&utm_medium=bottomtext_default&utm_campaign=premium)





Comment



More info




[Next Article](https://www.geeksforgeeks.org/array-subarray-subsequence-and-subset/?ref=next_article)

[Subarrays, Subsequences, and Subsets in Array](https://www.geeksforgeeks.org/array-subarray-subsequence-and-subset/?ref=next_article)



[A](https://www.geeksforgeeks.org/user/aayushi2402/contributions/?itm_source=geeksforgeeks&itm_medium=article_author&itm_campaign=auth_user)

[aayushi2402](https://www.geeksforgeeks.org/user/aayushi2402/contributions/?itm_source=geeksforgeeks&itm_medium=article_author&itm_campaign=auth_user)', 'Array is a linear data structure that is a collection of data elements of sametypes Arrays are stored in contiguous memory locations It is a staticdata structure with a fixed size', 'Applications, Advantages and Disadvantages of Array', 4, 'b15d3666-a77b-4098-89f1-225327c74f67', null);

-- DROP FUNCTION IF EXISTS get_lessons_and_learning_progress(uuid, uuid);

-- CREATE OR REPLACE FUNCTION get_lessons_and_learning_progress(
--     student_id UUID,
--     course2_id UUID
-- )
-- RETURNS TABLE(
-- 	learning_id UUID,
--     lesson_id UUID,
--     course_id UUID,
--     lesson_order INT,
--     lesson_name VARCHAR,
--     description TEXT,
--     content TEXT,
--     problem_id UUID,
--     exercise_id UUID,
--     status VARCHAR,
--     last_accessed_date TIMESTAMP WITH TIME ZONE
-- )
-- LANGUAGE plpgsql
-- AS $$
-- BEGIN
-- RETURN QUERY
-- SELECT
--     ll.learning_id,
--     l.lesson_id,
--     l.Course_ID,
--     l.Lesson_Order,
--     l.Lesson_Name,
--     l.Description,
--     l.Content,
--     l.Problem_ID,
--     l.Exercise_ID,
--     ll.Status,
--     ll.Last_accessed_date
-- FROM
--     Lessons l
--         JOIN
--     Learning_Lesson ll
--     ON
--         l.lesson_id = ll.lesson_id
-- WHERE
--     ll.user_id = student_id
--   AND l.course_id = course2_id
-- ORDER BY l.lesson_order;
-- END;
-- $$;
-- -- SELECT * FROM get_lessons_and_learning_progress(
-- --     'cabb0e34-1cdb-7818-5ab4-4018b7c81fe4',
-- --     'b15d3666-a77b-4098-89f1-225327c74f67'
-- -- );

-- DROP FUNCTION IF EXISTS caculate_lesson_count(uuid);
-- CREATE FUNCTION caculate_lesson_count (c_id UUID)
--     RETURNS Int AS $$
-- BEGIN

-- RETURN COALESCE((select count (*) from lessons where course_id = c_id), 0);
-- END;
-- $$ LANGUAGE plpgsql;

-- DROP FUNCTION IF EXISTS caculate_avg_review(uuid);
-- CREATE FUNCTION caculate_avg_review (c_id UUID)
--     RETURNS NUMERIC(5,2) AS $$
-- BEGIN

-- RETURN COALESCE((select avg(rating) from reviews where course_id = c_id), 0);
-- END;
-- $$ LANGUAGE plpgsql;

-- DROP FUNCTION IF EXISTS caculate_review_count(uuid);
-- CREATE FUNCTION caculate_review_count (c_id UUID)
--     RETURNS Int AS $$
-- BEGIN

-- RETURN COALESCE((select count(reviews.review_id) from reviews where course_id = c_id), 0);
-- END;
-- $$ LANGUAGE plpgsql;

-- DROP FUNCTION IF EXISTS get_latest_lesson(uuid, uuid);
-- CREATE FUNCTION get_latest_lesson (c_id UUID, user_uid UUID)
--     RETURNS UUID AS $$
-- BEGIN

-- RETURN
--     (select l.lesson_id
--      FROM
--          lessons l
--              JOIN
--          learning_lesson ll ON l.lesson_id = ll.lesson_id
--      WHERE
--          ll.user_id = user_uid
--        AND l.course_id = c_id
--      ORDER BY
--          ll.last_accessed_date DESC
--     LIMIT 1);
-- END;
-- $$ LANGUAGE plpgsql;

-- DROP FUNCTION IF EXISTS check_user_enrolled_course(uuid, uuid);
-- CREATE OR REPLACE FUNCTION check_user_enrolled_course(courseId UUID, userId UUID)
-- RETURNS BOOLEAN AS $$
-- DECLARE
-- exists BOOLEAN;
-- BEGIN
-- SELECT EXISTS (
--     SELECT 1
--     FROM user_courses
--     WHERE user_uid = userId
--       AND course_id = courseId
-- ) INTO exists;
-- RETURN exists;
-- END;
-- $$ LANGUAGE plpgsql;

-- DROP FUNCTION IF EXISTS calculate_completion_ratio(uuid, uuid);
-- CREATE OR REPLACE FUNCTION calculate_completion_ratio(courseId UUID, userId UUID)
-- RETURNS FLOAT AS $$
-- DECLARE
-- total_count INT;
--     done_theory_count INT;
-- 	done_practice_count INT;
--     completion_ratio FLOAT;
-- BEGIN
--     -- Calculate the total number of theories and practices
-- SELECT COUNT(*)
-- INTO total_count
-- FROM lessons l
--          JOIN learning_lesson ll ON l.lesson_id = ll.lesson_id
-- WHERE l.course_id = courseId
--   AND ll.user_id = userId;

-- -- Calculate the number of completed theories
-- SELECT COUNT(*)
-- INTO done_theory_count
-- FROM lessons l
--          JOIN learning_lesson ll ON l.lesson_id = ll.lesson_id
-- WHERE l.course_id = courseId
--   AND ll.user_id = userId
--   AND (ll.is_done_theory IS TRUE);

-- -- Calculate the number of completed theories and practices
-- SELECT COUNT(*)
-- INTO done_practice_count
-- FROM lessons l
--          JOIN learning_lesson ll ON l.lesson_id = ll.lesson_id
-- WHERE l.course_id = courseId
--   AND ll.user_id = userId
--   AND (ll.is_done_practice IS TRUE);

-- -- Calculate the completion ratio
-- IF total_count > 0 THEN
--         completion_ratio := (done_theory_count::FLOAT + done_practice_count::FLOAT) * 100 / (total_count::FLOAT * 2);
-- ELSE
--         completion_ratio := 0;
-- END IF;

-- RETURN completion_ratio;
-- END;
-- $$ LANGUAGE plpgsql;

-- DROP FUNCTION IF EXISTS get_details_course(uuid, uuid);
-- CREATE OR REPLACE FUNCTION get_details_course(
--     courseId UUID,
-- 	userId UUID
-- )
-- RETURNS TABLE(
-- 	course_id UUID,
--     course_logo TEXT,
--     course_name VARCHAR(255),
--     description TEXT,
--     level VARCHAR(20),
--     price NUMERIC(11,2),
--     unit_price VARCHAR(10),
--     user_uid UUID,
--     lesson_count INT,
--     average_rating NUMERIC(5,2),
--     review_count INT,
-- 	is_user_enrolled BOOLEAN,
-- 	latest_lesson_id UUID,
-- 	progress_percent FLOAT
-- )
-- LANGUAGE plpgsql
-- AS $$
-- BEGIN
-- RETURN QUERY
-- SELECT
--     c.course_id,
--     c.course_logo,
--     c.course_name,
--     c.description,
--     c.level,
--     c.price,
--     c.unit_price,
--     c.user_uid,
--     caculate_lesson_count(c.course_id) AS lesson_count,
--     caculate_avg_review(c.course_id) as average_rating,
--     caculate_review_count(c.course_id) as review_count,
--     check_user_enrolled_course(c.course_id, userId) as is_user_enrolled,
--     get_latest_lesson(c.course_id, userId) as latest_lesson_id,
--     calculate_completion_ratio(c.course_id, userId) as progress_percent
-- FROM
--     Courses c
-- WHERE
--     c.course_id = courseId;
-- END;
-- $$;

-- DROP FUNCTION IF EXISTS check_is_done_theory(uuid);
-- CREATE OR REPLACE FUNCTION check_is_done_theory(learningId UUID)
-- RETURNS BOOLEAN AS $$
-- DECLARE
-- exists BOOLEAN;
-- BEGIN
-- SELECT EXISTS (
--     SELECT 1
--     FROM assignments
--     WHERE learning_id = learningId
-- ) INTO exists;
-- RETURN exists;
-- END;
-- $$ LANGUAGE plpgsql;

-- DROP FUNCTION IF EXISTS get_next_lesson_id(uuid, uuid);
-- CREATE OR REPLACE FUNCTION get_next_lesson_id(courseId UUID, userId UUID)
-- RETURNS UUID AS $$
-- BEGIN
-- RETURN
--     (SELECT
--          l.lesson_id
--      FROM
--          learning_lesson ll
--              JOIN
--          lessons l
--          ON ll.lesson_id = l.lesson_id
--      WHERE
--          ll.user_id = userId
--        AND ll.status = 'NEW'
--        AND l.course_id = courseId
--      ORDER BY
--          l.lesson_order
--     LIMIT 1);

-- IF NOT FOUND THEN
--         RETURN (SELECT NULL::UUID);
-- END IF;
-- END;
-- $$ LANGUAGE plpgsql;


-- DROP FUNCTION IF EXISTS get_next_lesson_name(uuid, uuid);
-- CREATE OR REPLACE FUNCTION get_next_lesson_name(courseId UUID, userId UUID)
-- RETURNS VARCHAR(255) AS $$
-- BEGIN
-- RETURN
--     (SELECT
--          l.lesson_name
--      FROM
--          learning_lesson ll
--              JOIN
--          lessons l
--          ON ll.lesson_id = l.lesson_id
--      WHERE
--          ll.user_id = userId
--        AND ll.status = 'NEW'
--        AND l.course_id = courseId
--      ORDER BY
--          l.lesson_order
--     LIMIT 1);

-- IF NOT FOUND THEN
--         RETURN (SELECT NULL::VARCHAR(255));
-- END IF;
-- END;
-- $$ LANGUAGE plpgsql;

-- DROP FUNCTION IF EXISTS get_details_lesson(uuid, uuid);
-- CREATE OR REPLACE FUNCTION get_details_lesson(
--     lessonId UUID,
-- 	userId UUID
-- )
-- RETURNS TABLE(
-- 	lesson_id UUID,
--     content TEXT,
--     description TEXT,
-- 	lesson_order INT,
--     lesson_name VARCHAR(255),
--     course_id UUID,
--     exercise_id UUID,
-- 	learning_id UUID,
--     next_lesson_id UUID,
--     next_lesson_name VARCHAR(255),
--     is_done_theory BOOLEAN,
-- 	is_done_practice BOOLEAN
-- )
-- LANGUAGE plpgsql
-- AS $$
-- BEGIN
-- RETURN QUERY
-- SELECT
--     l.lesson_id,
--     l.content,
--     l.description,
--     l.lesson_order,
--     l.lesson_name,
--     l.course_id,
--     l.exercise_id,
--     lu.learning_id,
--     get_next_lesson_id(l.course_id, userId) as next_lesson_id,
--     get_next_lesson_name(l.course_id, userId) AS next_lesson_name,
--     check_is_done_theory(lu.learning_id) as is_done_theory,
--     false as is_done_practice
-- FROM Lessons AS l
--          LEFT JOIN
--      (SELECT
--           ll.learning_id,
--           ll.lesson_id
--       FROM Learning_lesson AS ll
--       WHERE ll.user_id = userId) as lu
--      ON	lu.lesson_id = l.lesson_id
-- WHERE l.lesson_id = lessonId;
-- END;
-- $$;

-- DROP PROCEDURE IF EXISTS mark_theory_of_lesson_as_done(uuid, uuid);

-- CREATE OR REPLACE PROCEDURE mark_theory_of_lesson_as_done(
--     IN learningID UUID,
--     IN exerciseId UUID,
--     OUT is_done_theory BOOLEAN
-- )
-- LANGUAGE plpgsql
-- AS $$
-- BEGIN
-- SELECT EXISTS (
--     SELECT 1
--     FROM
--         (SELECT
--              ad.answer,
--              ad.question_id
--          FROM
--              learning_lesson ll
--                  JOIN
--              assignments as a
--              ON ll.learning_id = a.learning_id
--                  JOIN
--              assignment_details as ad
--              ON ad.assignment_id = a.assignment_id
--          WHERE
--              ll.learning_id = learningID) AS ans
--             JOIN
--         (SELECT
--              Q.question_id,
--              Q.correct_answer
--          FROM exercises AS e
--                   JOIN question_list as QL
--                        ON e.exercise_id = QL.exercise_id
--                   JOIN questions AS Q
--                        ON Q.question_id = QL.question_id
--          WHERE e.exercise_id = exerciseId) AS res
--         ON res.question_id = ans.question_id
--             AND res.correct_answer = ans.answer
-- ) INTO is_done_theory;
-- END;
-- $$;