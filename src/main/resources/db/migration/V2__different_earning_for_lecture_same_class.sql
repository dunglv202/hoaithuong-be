ALTER TABLE lecture ADD teacher_earning INT NULL;

UPDATE lecture
JOIN tutor_class ON lecture.tutor_class_id = tutor_class.id
SET lecture.teacher_earning = tutor_class.pay_for_lecture
WHERE lecture.teacher_earning IS NULL;

ALTER TABLE lecture MODIFY teacher_earning INT NOT NULL;