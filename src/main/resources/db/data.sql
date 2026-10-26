-- =====================================================================
-- 샘플 데이터 (유료 게시글 구매 기능 단계 기준)
-- 전제 : User.point, Board.premium 필드와 Purchase 엔티티(purchase_tb)가 만들어져 있어야 합니다
-- 비밀번호는 모두 "1234" 를 BCrypt 로 해싱한 값입니다
-- 게시글 content 컬럼은 길이 지정이 없어 VARCHAR(255) 입니다. 내용은 255자 이내로 작성합니다
-- =====================================================================

-- 1. 사용자 (외래키가 가리킬 대상이므로 먼저 넣습니다)
-- point : admin 0, minsu 1000(구매 테스트), jihun 500(딱 1번 구매 가능), seoyeon 0(포인트 부족 테스트)
INSERT INTO user_tb (username, password, email, point, created_at) VALUES ('admin',   '$2a$10$jiByiZruBRyjQuhMgF6kY.Hg2NTqKV2YqmbiGoUYn/HthKtJrkoca', 'admin@tenco.com',   0,    NOW());
INSERT INTO user_tb (username, password, email, point, created_at) VALUES ('minsu',   '$2a$10$jiByiZruBRyjQuhMgF6kY.Hg2NTqKV2YqmbiGoUYn/HthKtJrkoca', 'minsu@tenco.com',   1000, NOW());
INSERT INTO user_tb (username, password, email, point, created_at) VALUES ('jihun',   '$2a$10$jiByiZruBRyjQuhMgF6kY.Hg2NTqKV2YqmbiGoUYn/HthKtJrkoca', 'jihun@tenco.com',   500,  NOW());
INSERT INTO user_tb (username, password, email, point, created_at) VALUES ('seoyeon', '$2a$10$jiByiZruBRyjQuhMgF6kY.Hg2NTqKV2YqmbiGoUYn/HthKtJrkoca', 'seoyeon@tenco.com', 0,    NOW());

-- 1-1. 사용자 역할 (admin 은 관리자이면서 일반 사용자)
INSERT INTO user_role_tb (user_id, role) VALUES (1, 'ADMIN');
INSERT INTO user_role_tb (user_id, role) VALUES (1, 'USER');
INSERT INTO user_role_tb (user_id, role) VALUES (2, 'USER');
INSERT INTO user_role_tb (user_id, role) VALUES (3, 'USER');
INSERT INTO user_role_tb (user_id, role) VALUES (4, 'USER');

-- 2. 게시글 (user_id 는 위 사용자의 id, premium 이 true 이면 유료 게시글)
-- 1~8번은 기존 게시글(번호 유지), 9~14번은 페이징, 검색, 유료 글 테스트용 추가 게시글
-- 유료 게시글 : 7번(jihun), 9번(minsu), 12번(seoyeon)

-- admin(1)
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('블로그 개설을 환영합니다', '새 블로그가 문을 열었습니다. 개발 공부 기록, 프로젝트 후기, 취업 정보 등 자유롭게 글을 남겨 주세요. 좋은 글에는 댓글로 응원도 부탁드립니다. 앞으로 유료 게시글과 포인트 기능도 차례로 추가될 예정입니다.', 1, false, NOW());
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('이용 수칙 안내', '서로 존중하는 소통 문화를 함께 만들어 가요. 1. 욕설과 비방은 삭제됩니다. 2. 다른 사람의 글을 허락 없이 옮기지 않습니다. 3. 광고성 글은 예고 없이 삭제될 수 있습니다. 4. 질문 글에는 시도해 본 코드와 오류 메시지를 함께 적어 주세요.', 1, false, NOW());

-- minsu(2)
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('스프링 부트 학습 후기', 'JPA를 배우니 SQL을 직접 쓰는 일이 확 줄었습니다. 처음에는 영속성 컨텍스트와 더티 체킹이 낯설었는데, 엔티티 값만 바꿔도 커밋 시점에 UPDATE 가 나가는 것을 콘솔 로그로 직접 확인하고 나서 이해가 됐습니다. 다음 목표는 JPQL 과 페이징입니다.', 2, false, NOW());
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('연관관계 정리 노트', '@ManyToOne 과 외래키의 관계를 정리해 봤습니다. 1:N 관계에서 외래키는 항상 N 쪽 테이블에 있습니다. 게시글과 사용자라면 board_tb 에 user_id 가 생깁니다. 지연 로딩(LAZY)은 실제로 사용할 때 조회하므로, 목록에서 작성자가 필요하면 JOIN FETCH 로 한 번에 가져옵니다.', 2, false, NOW());
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('코딩 테스트 문제 추천', '알고리즘 공부에 좋은 문제들을 모아 봤습니다. 배열과 문자열 문제로 시작해서 해시, 스택과 큐, 정렬, 완전 탐색, BFS 와 DFS 순서로 풀어 보세요. 하루 2문제씩 꾸준히 푸는 것이 한 번에 몰아서 푸는 것보다 훨씬 효과가 좋았습니다.', 2, false, NOW());

-- jihun(3)
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('프론트엔드 프레임워크 비교', '리액트와 뷰의 장단점을 비교해 봤습니다. 리액트는 생태계가 넓고 채용 공고가 많습니다. 뷰는 문법이 단순해서 처음 배우기 쉽습니다. 둘 다 컴포넌트 단위로 화면을 나누는 방식은 같으므로 하나를 제대로 익히면 다른 하나도 금방 배울 수 있습니다.', 3, false, NOW());
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('신입 개발자 취업 팁', '포트폴리오에서 가장 중요했던 점을 공유합니다. 기능 목록보다 내가 겪은 문제와 해결 과정을 적는 것이 면접에서 훨씬 많이 질문받았습니다. 예를 들어 N+1 문제를 어떻게 발견했고 JOIN FETCH 로 쿼리 수를 몇 개에서 몇 개로 줄였는지처럼 숫자로 설명하면 좋습니다.', 3, true, NOW());

-- seoyeon(4)
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('첫 번째 게시글입니다', '블로그에 처음 글을 올려 봅니다. 비전공자로 개발 공부를 시작한 지 세 달째입니다. 자바 문법을 지나 이제 스프링 부트로 게시판을 만들고 있습니다. 공부하면서 막혔던 부분과 해결한 방법을 꾸준히 기록해 보려고 합니다. 자주 소통해요.', 4, false, NOW());

-- 추가 게시글 (9~14번)
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('JPA N+1 문제와 JOIN FETCH 정리', '게시글 목록 10개를 조회했더니 작성자 조회 쿼리가 10번 더 나가는 N+1 문제를 겪었습니다. LAZY 로 설정한 user 를 화면에서 꺼낼 때마다 SELECT 가 실행되기 때문입니다. JPQL 에 JOIN FETCH b.user 를 추가해 쿼리 1번으로 해결한 과정과 콘솔 로그 비교를 정리했습니다.', 2, true, NOW());
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('Git 브랜치 전략 정리', '팀 프로젝트에서 사용한 브랜치 전략을 정리했습니다. main 은 배포용, develop 은 통합용으로 두고 기능마다 feature 브랜치를 만들어 작업했습니다. 작업이 끝나면 Pull Request 로 코드 리뷰를 받고 develop 에 합칩니다. 커밋 메시지 규칙도 함께 정하면 히스토리를 보기 편합니다.', 3, false, NOW());
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('자바 컬렉션 한눈에 보기', 'List, Set, Map 의 차이를 표로 정리했습니다. List 는 순서가 있고 중복을 허용합니다. Set 은 중복을 허용하지 않습니다. Map 은 키와 값을 한 쌍으로 저장합니다. 실무에서는 ArrayList, HashSet, HashMap 을 가장 많이 쓰고, 순서가 필요하면 LinkedHashMap 을 씁니다.', 4, false, NOW());
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('기술 면접 예상 질문 모음', '실제 면접에서 받은 질문을 모았습니다. 1. 트랜잭션의 ACID 를 설명해 주세요. 2. 세션과 쿠키의 차이는 무엇인가요. 3. JPA 의 영속성 컨텍스트가 주는 이점은 무엇인가요. 4. N+1 문제는 왜 생기고 어떻게 해결하나요. 5. REST API 설계 원칙을 말해 주세요. 질문마다 30초 답변 예시도 붙였습니다.', 4, true, NOW());
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('서버 점검 안내', '보다 안정적인 서비스를 위해 서버 점검을 진행합니다. 점검 시간에는 글쓰기와 댓글 작성이 잠시 제한됩니다. 작성 중인 글이 있다면 미리 저장해 주세요. 점검이 끝나면 공지로 다시 알려 드리겠습니다. 이용에 불편을 드려 죄송합니다.', 1, false, NOW());
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('포트폴리오 README 작성법', 'README 는 면접관이 가장 먼저 보는 문서입니다. 프로젝트 소개 한 줄, 사용 기술, 실행 방법, 화면 캡처, ERD, 트러블 슈팅 순서로 작성하는 것을 추천합니다. 특히 트러블 슈팅은 문제 상황, 원인, 해결 방법, 결과를 나눠서 적으면 읽기 쉽습니다.', 3, false, NOW());

-- 추가 유료 게시글 (15~18번)
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('스프링 부트 프로젝트 배포 가이드', '완성한 프로젝트를 서버에 올리는 과정을 단계별로 정리했습니다. jar 파일 빌드, 서버에 자바 설치, 프로필별 설정 분리, 환경 변수로 비밀 키 관리, 백그라운드 실행까지 다룹니다. 배포 후 자주 만나는 포트 충돌과 DB 접속 오류 해결 방법도 함께 적었습니다.', 1, true, NOW());
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('세션 로그인 구현기', '스프링 시큐리티 없이 HttpSession 으로 로그인을 구현한 과정입니다. 로그인 성공 시 세션에 사용자 정보를 저장하고, 인터셉터로 로그인이 필요한 주소를 한 곳에서 검사했습니다. 비밀번호는 BCrypt 로 해싱하고, 세션에는 비밀번호를 담지 않도록 DTO 를 따로 만들었습니다.', 2, true, NOW());
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('토이 프로젝트 아이디어 모음', '포트폴리오용 토이 프로젝트 아이디어를 모았습니다. 스터디 출석부, 중고 거래 게시판, 맛집 지도, 가계부, 도서 대여 관리, 운동 기록 앱 등입니다. 아이디어마다 필요한 테이블 구성과 꼭 넣으면 좋은 기능(검색, 페이징, 파일 업로드, 결제)을 함께 정리했습니다.', 3, true, NOW());
INSERT INTO board_tb (title, content, user_id, premium, created_at) VALUES ('SQL 조인 완벽 정리', 'INNER JOIN, LEFT JOIN, RIGHT JOIN 의 차이를 예제 테이블로 정리했습니다. 게시글과 댓글 테이블로 댓글이 없는 게시글까지 보려면 왜 LEFT JOIN 이 필요한지 결과 표로 비교했습니다. JPQL 의 JOIN FETCH 와 SQL 조인이 어떻게 연결되는지도 마지막에 설명했습니다.', 4, true, NOW());

-- 3. 댓글 (board_id 는 게시글 id, user_id 는 작성자 id)
-- 1번 게시글 : 블로그 개설을 환영합니다 (admin)
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('축하드립니다! 자주 방문할게요.', 1, 2, NOW());
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('좋은 글 많이 올라오면 좋겠네요.', 1, 3, NOW());
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('개설 축하드려요!', 1, 4, NOW());

-- 2번 게시글 : 이용 수칙 안내 (admin)
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('이용 수칙 잘 읽었습니다.', 2, 2, NOW());

-- 3번 게시글 : 스프링 부트 학습 후기 (minsu)
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('저도 JPA 배우는 중인데 공감합니다.', 3, 1, NOW());
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('후기 공유 감사합니다!', 3, 3, NOW());

-- 4번 게시글 : 연관관계 정리 노트 (minsu)
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('외래키는 N 쪽에 있다는 게 핵심이네요.', 4, 1, NOW());
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('정리 덕분에 이해했습니다.', 4, 4, NOW());

-- 6번 게시글 : 프론트엔드 프레임워크 비교 (jihun)
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('리액트를 먼저 배워 보려고 합니다.', 6, 2, NOW());

-- 7번 게시글 : 신입 개발자 취업 팁 (jihun, 유료)
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('포트폴리오 팁 정말 유용해요.', 7, 4, NOW());
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('면접 준비 이야기도 들려주세요.', 7, 2, NOW());

-- 9번 게시글 : JPA N+1 문제와 JOIN FETCH 정리 (minsu, 유료)
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('구매해서 읽었는데 콘솔 로그 비교가 특히 좋았습니다.', 9, 3, NOW());

-- 10번 게시글 : Git 브랜치 전략 정리 (jihun)
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('저희 팀도 feature 브랜치 방식으로 진행 중입니다.', 10, 4, NOW());
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('커밋 메시지 규칙 예시도 올려 주세요.', 10, 2, NOW());

-- 11번 게시글 : 자바 컬렉션 한눈에 보기 (seoyeon)
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('표로 보니 차이가 바로 이해됩니다.', 11, 2, NOW());

-- 14번 게시글 : 포트폴리오 README 작성법 (jihun)
INSERT INTO reply_tb (comment, board_id, user_id, created_at) VALUES ('트러블 슈팅 항목 바로 추가하러 갑니다.', 14, 4, NOW());

-- 5, 8, 12, 13번 게시글은 댓글 없음 (댓글이 없을 때 화면 확인용)

-- 4. 구매 내역 (user_id 가 board_id 게시글을 price 포인트로 구매)
-- 같은 사용자가 같은 글을 두 번 살 수 없고(uk_user_board), 본인 글은 구매하지 않는다
-- point 값은 구매 후 "현재 남은 포인트" 이므로 따로 맞출 필요 없음
--
-- 아직 구매하지 않은 유료 글 (직접 구매 테스트용)
--   minsu(1000 P)  : 7, 12, 17, 18번 → 2개까지 구매 가능
--   jihun(500 P)   : 12, 15, 18번   → 1개만 구매 가능
--   seoyeon(0 P)   : 16, 17번       → 포인트 부족 확인
--   admin(0 P)     : 9, 16, 17번    → 포인트 부족 확인
-- 삭제 막기 확인 : 17번(jihun)만 구매자가 없어 삭제 가능, 나머지 유료 글은 삭제 불가

-- jihun(3) : minsu 의 9번, 16번 구매
INSERT INTO purchase_tb (user_id, board_id, price, created_at) VALUES (3, 9,  500, NOW());
INSERT INTO purchase_tb (user_id, board_id, price, created_at) VALUES (3, 16, 500, NOW());
-- seoyeon(4) : 7번(jihun), 9번(minsu), 15번(admin) 구매
INSERT INTO purchase_tb (user_id, board_id, price, created_at) VALUES (4, 7,  500, NOW());
INSERT INTO purchase_tb (user_id, board_id, price, created_at) VALUES (4, 9,  500, NOW());
INSERT INTO purchase_tb (user_id, board_id, price, created_at) VALUES (4, 15, 500, NOW());
-- admin(1) : 7번(jihun), 12번(seoyeon), 18번(seoyeon) 구매
INSERT INTO purchase_tb (user_id, board_id, price, created_at) VALUES (1, 7,  500, NOW());
INSERT INTO purchase_tb (user_id, board_id, price, created_at) VALUES (1, 12, 500, NOW());
INSERT INTO purchase_tb (user_id, board_id, price, created_at) VALUES (1, 18, 500, NOW());
-- minsu(2) : 15번(admin) 구매
INSERT INTO purchase_tb (user_id, board_id, price, created_at) VALUES (2, 15, 500, NOW());