-- 1. 사용자 (외래키가 가리킬 대상이므로 먼저 넣습니다)
-- 비밀번호는 지금은 연습용 평문입니다. 암호화는 나중에 적용합니다
INSERT INTO user_tb (username, password, email, created_at) VALUES ('admin',   '1234', 'admin@tenco.com',   NOW());
INSERT INTO user_tb (username, password, email, created_at) VALUES ('minsu',   '1234', 'minsu@tenco.com',   NOW());
INSERT INTO user_tb (username, password, email, created_at) VALUES ('jihun',   '1234', 'jihun@tenco.com',   NOW());
INSERT INTO user_tb (username, password, email, created_at) VALUES ('seoyeon', '1234', 'seoyeon@tenco.com', NOW());

-- 2. 게시글 (user_id 는 위 사용자의 id)
-- admin(1)
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('블로그 개설을 환영합니다', '새 블로그가 문을 열었습니다. 많은 참여 부탁드립니다.', 1, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('이용 수칙 안내', '서로 존중하는 소통 문화를 함께 만들어 가요.', 1, NOW());
-- minsu(2)
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('스프링 부트 학습 후기', 'JPA를 배우니 SQL을 직접 쓰는 일이 확 줄었습니다.', 2, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('연관관계 정리 노트', '@ManyToOne 과 외래키의 관계를 정리해 봤습니다.', 2, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('코딩 테스트 문제 추천', '알고리즘 공부에 좋은 문제들을 모아 봤습니다.', 2, NOW());
-- jihun(3)
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('프론트엔드 프레임워크 비교', '리액트와 뷰의 장단점을 비교해 봤습니다.', 3, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('신입 개발자 취업 팁', '포트폴리오에서 가장 중요했던 점을 공유합니다.', 3, NOW());
-- seoyeon(4)
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('첫 번째 게시글입니다', '블로그에 처음 글을 올려 봅니다. 자주 소통해요.', 4, NOW());