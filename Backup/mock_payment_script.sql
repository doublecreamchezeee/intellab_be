INSERT INTO public.users(uuid, uid)
VALUES ('1974945b-162f-c609-9934-c330126caf14','FT38VBSnmKUaCKuA6QwV8voS6NJ2'),
('2e6402f0-d2b0-30ff-dc2e-53e9666d6f1d','cAI9FPriFaYdRQR41bx47DfO8mY2'),
('573a0d23-196a-6b35-cf9c-6e168c18596a','ZqrT4hQ0yLa3QlwZZITY2CQ6txG2'),
('59208f4f-d6ae-0792-0317-f36822578f81','M7OzKUnPmraVgbyvKTgDtG35lLq1'),
('64ce3c40-114f-8f4b-50f5-d737cccc2c87','Dg12bGtwJHarGR8am1f9jleense2'),
('6b1fd807-2f7b-77a2-ea02-5530cadbe946','MIRKJL5khfa0PyzpKP2khJrLy6f1'),
('6eaea212-5351-45c3-3a53-9c9b9a407e1e','8jUtjY22UYTEKxglmbHeUTIuHBu2'),
('6f00f4cb-69c8-f3fc-23fe-dcdc65edfddb','KJAUoOuLbFTUrNLOSHGIzYjI5Z63'),
('8fdd9c28-78e4-b5b4-741d-ccfaec3aa7b6','oPoMnzU4ucdCRuhiGophhnL3Stp2'),
('ad9f9995-484b-9a12-8c25-7bd32527ab3d','nFmwM1SHRFdGLodbmXvKUOqyUoK2'),
('aef57c4e-4011-1938-0086-86b46a63617c','nCirq4T53BPtub8VnU5VQas14B12'),
('b70339e8-2c3b-1a70-5d18-52ed64ae4dc2','iod7SPGlxWUa3JcyCjc1eoSiaBM2'),
('bfa6012d-ce4b-c982-3de6-0d622af861ca','HK0KvF5fZzUYRdAyEzb1gNWjiU62'),
('cabb0e34-1cdb-7818-5ab4-4018b7c81fe4','97UUEvOrXiRreXMyDx5KtSapPIx2'),
('e40c0cd8-ec50-df48-8ef2-2562a603ef99','ZXUBibqcgxXnRJTiiSvXqJ2DNbc2'),
('e4db16ca-ecf9-cf22-bcc9-b3eddfbe3cc9','WgPbP365QOfkZSfEmu6WG0Tpntx2'),
('f42a0a9b-1e19-ea1e-f843-7b3304f41d5c','MmAkaDuTU6PveDxSNEGvMg2QLsF3'),
('f9191f86-e66d-e59f-6eea-d0a8092d54a0','4oXW9av7xuULl3CwRIU6PU0rcUm1');



CREATE OR REPLACE PROCEDURE insert_multiple_vnpay_payments(n INT)
LANGUAGE plpgsql
AS $$
DECLARE
  i INT;
  rand_user RECORD;
  rand_course RECORD;
  new_payment_id UUID;
  v_created_at TIMESTAMP;
  v_received_at TIMESTAMP;
  rand_transaction_no TEXT;
  start_date TIMESTAMP := '2025-01-01 00:00:00';
BEGIN
  FOR i IN 1..n LOOP
    -- Lấy ngẫu nhiên 1 user
    SELECT u.uid as user_uid, u.uuid as user_uuid, course_id
    INTO rand_user
    FROM users u,courses c
	where c.price > 0 
			and c.course_id not in (	select vp.course_id 
									from vnpay_payment_courses vp
									where c.course_id = vp.course_id 
											and vp.user_uid = u.uid)
    ORDER BY random()
    LIMIT 1;

    -- Lấy ngẫu nhiên 1 course
    SELECT c.course_id, c.course_name, c.price
    INTO rand_course
    FROM courses c
	WHERE c.course_id = rand_user.course_id
    ORDER BY random()
    LIMIT 1;

    -- Sinh ID và timestamps
    v_created_at := start_date + (random() * EXTRACT(EPOCH FROM (now() - start_date))) * interval '1 second';	
	v_received_at := v_created_at + (random() * interval '10 seconds');
	rand_transaction_no := floor(10000000 - (random() * 1000000))::TEXT;

    -- Chèn vào bảng vnpay_payment
    INSERT INTO public.vnpay_payment (user_uid, user_uuid,
      transaction_status, response_code,
      total_payment_amount, currency,
      paid_amount, bank_code, transaction_reference,
      created_at, received_at, bank_transaction_no,
      transaction_no, order_description, payment_for,
      payment_premium_package_id
    )
    VALUES (
      rand_user.user_uid, rand_user.user_uuid,
      '00', '00',
      rand_course.price, 'VND',
      rand_course.price, 'VNBANK', rand_transaction_no,
      v_created_at, v_received_at, 'VNBANK' || rand_transaction_no,
      rand_transaction_no , 'Thanh toan cho khoa hoc: ' || rand_course.course_name, 'Course',
      NULL
    ) returning payment_id into new_payment_id;

    -- Chèn vào bảng vnpay_payment_courses
    INSERT INTO public.vnpay_payment_courses (
      course_id, user_uid, payment_id
    )
    VALUES (
      rand_course.course_id, rand_user.user_uid, new_payment_id
    );
  END LOOP;
END;
$$;


CREATE OR REPLACE PROCEDURE insert_multiple_vnpay_payments_for_premium(n INT)
LANGUAGE plpgsql
AS $$
DECLARE
  i INT;
  rand_user RECORD;
  new_payment_id UUID;
  v_created_at TIMESTAMP;
  v_received_at TIMESTAMP;
  plan INT;
  rand_transaction_no TEXT;
  v_status VARCHAR;
  v_end_date TIMESTAMP;
  v_paid REAL;
  v_start_date TIMESTAMP := '2022-01-01 00:00:00';
BEGIN
  FOR i IN 1..n LOOP
    -- Lấy ngẫu nhiên 1 user
    SELECT uid as user_uid, uuid as user_uuid
    INTO rand_user
    FROM users
    ORDER BY random()
    LIMIT 1;

	SELECT (ARRAY[30, 365])[floor(random() * 2 + 1)::int] INTO plan;
    -- random data
    v_created_at := v_start_date + (random() * EXTRACT(EPOCH FROM (now() - v_start_date))) * interval '1 second';	
	v_end_date := v_created_at + plan*INTERVAL '1 days';
	
	rand_transaction_no := floor(random() * 1000000)::TEXT;

	WHILE EXISTS(	SELECT 1 FROM vnpay_payment_premium_package vnp 
					WHERE ((v_created_at BETWEEN start_date AND end_date) 
								OR ((v_end_date BETWEEN start_date AND end_date))) 
							AND rand_user.user_uuid = user_uuid
					) LOOP
		v_created_at := v_created_at - plan*INTERVAL '1 days';
		v_end_date := v_created_at + plan*INTERVAL '1 days';
	END LOOP;
				
	v_received_at := v_created_at + (random() * interval '10 seconds');
	

	IF (v_end_date < now()) THEN
		v_status := 'Expired';
	ELSE
		v_status := 'Active';
	END IF;

	IF (plan = 30) THEN
		v_paid := 499000;
	ELSE
		v_paid := 3588000;
	END IF;


	INSERT INTO public.vnpay_payment_premium_package(
	user_uuid, user_uid, 
	start_date, end_date, 
	package_type, status, duration)
	VALUES (rand_user.user_uuid, rand_user.user_uid,
	v_received_at, v_end_date,
	'PREMIUM_PLAN', v_status, plan
	) 
	returning payment_premium_package_id into new_payment_id;
	

    -- Chèn vào bảng vnpay_payment
    INSERT INTO public.vnpay_payment (user_uid, user_uuid,
      transaction_status, response_code,
      total_payment_amount, currency,
      paid_amount, bank_code, transaction_reference,
      created_at, received_at, bank_transaction_no,
      transaction_no, order_description, payment_for,
      payment_premium_package_id
    )
    VALUES (
      rand_user.user_uid, rand_user.user_uuid,
      '00', '00',
      v_paid, 'VND',
      v_paid, 'VNBANK', rand_transaction_no,
      v_created_at, v_received_at, 'VNBANK' || rand_transaction_no,
      rand_transaction_no , 'Thanh toan cho nang cap tai khoan: Premium Plan', 'Subscription',
      new_payment_id
    );

  END LOOP;
END;
$$;


call insert_multiple_vnpay_payments(252)

call insert_multiple_vnpay_payments_for_premium(100)



