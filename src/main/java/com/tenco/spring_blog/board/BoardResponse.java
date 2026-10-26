package com.tenco.spring_blog.board;


import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * 게시글 응답 DTO(실무에서 많이 사용됨)
 * - 서비스(트랜잭션 안)에서 엔티티를 DTO로 변환해서 반환한다.
 * - 뷰는 엔티티가 아닌 DTO만 사용하므로 OSIV를 꺼도 지연 로딩 오류가 나지 않는다.
 * - 화면에 필요한 값만 담으므로 계층간에 결합도도 낮아진다.
 */
public class BoardResponse {

    // 게시글 목록 + 페이지 정보 응답 DTO
    @Data
    public static class PageDto {
        private List<ListDto> content; //현재 페이지의 게시글 목록
        private int size; // 페이징 처리 계산을 위한 처리
        private int currentPage; // 현재 페이지(1부터 시작)
        private int totalPages; // 전체 페이지수
        private long totalElements; // 전체 게시글수
        private boolean hasPrevious; //이전 페이지 존재 여부
        private boolean hasNext; // 다음 페이지 존재 여부
        private List<PageLink> pagesLinks; //화면에 표시할 페이지 번호 목록 -> 자동 페이지 계산을 위한 변수

        private int previousPage;

        //nextPage 관련 변수
        private int nextPage;

        // 생성자 추가
        public PageDto(Page<Board> page) {
            this.content = page.getContent().stream()
                    .map(ListDto::new)
                    .toList();

            this.size = page.getSize();
            this.currentPage = page.getNumber() + 1; // 스프링의 페이지는 0번 부터 시작하기에 +1 처리
            this.totalPages = page.getTotalPages();
            this.totalElements = page.getTotalElements();
            this.hasPrevious = page.hasPrevious();
            this.hasNext = page.hasNext();
            this.previousPage = currentPage - 1;
            this.nextPage = currentPage + 1;

            this.pagesLinks = makePageLinks(currentPage, totalPages); //동적으로 데이터를 만들어주어야 함
        }

        // 편의 메서드 구현 : 현재 페이지를 가운데 두고 앞뒤 2개씩 최대 5개의 페이지링크 생성
        private List<PageLink> makePageLinks(int currentPage, int totalPages) {
            List<PageLink> links = new ArrayList<>();

            // 첫번째 시나리오
            // 현재 페이지가 5일 경우 첫 번째 페이지는 3
            int startPage = Math.max(1, currentPage - 2); //1보다 작아지지 않게하는 코드
            // 마지막 페이지
            int endPage = Math.min(totalPages, currentPage + 2); // 전체페이지를 넘지 않게

            for (int i = startPage; i <= endPage; i++) {
                links.add(new PageLink(i, i == currentPage)); //여기서 i는 active를 의미한다.
            }
            return links;

        }

    }// end of pageDto

    // 페이지 번호 버튼 생성(하나의 정보)
    @Data
    @AllArgsConstructor
    public static class PageLink {
        // 고려할 점 1. 속성  2. 활성화되어있는 페이지인가?
        private int number; // 표시할 페이지 번호
        private boolean active; // 현재 페이지 여부 (강조 표시용)

    }


    @Data
    public static class ListDto {
        private Long id;
        private String title;
        private String username;
        private String createdAt;

        public ListDto(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            // JOIN FETCH로 이미 가져온 user 사용할 예정(추가 쿼리 발생 없음)
            if (board.getUser() != null) {
                this.username = board.getUser().getUsername();
            }
            this.createdAt = board.getTime();
        }
    }


    // 게시글 상세 응답 DTO
    @Data
    public static class DetailDto {
        private Long id;
        private String title;
        private String content;
        private Long userId;
        private String username;
        private String createdAt;
        private boolean premium; //유료 게시글 여부
        private int price; // 가격 안내 문구용
        private boolean owner; // 로그인한 사용자가 작성자인가?
        private boolean purchased; // 로그인한 이용자가 구매를 했는가?
        private boolean canRead; //본문 열람 가능 여부 -> 1. 무료글일 경우 - 모두 열람 가능 2. 유료게시글일 경우 - 작성자, 구매자 -> 열람 가능

        public DetailDto(Board board, boolean owner, boolean purchased) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
            // 지연 로딩 오류 방지를 위한 방어적 코드 작성
            if (board.getUser() != null) {
                this.userId = board.getUser().getId();
                this.username = board.getUser().getUsername();
            }
            if (board.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(board.getCreatedAt());
            }
            this.premium = board.isPremium();
            this.price = 500;  // todo - 추후 수정
            this.owner = owner;
            this.purchased = purchased;
            this.canRead = !premium || owner || purchased;
            //중요) 그렇지만 content는 어차피 화면에 나타나는 것이기 때문에 어떠한 조작으로도 게시글을 읽을 수 있는 문제가 발생한다.
            // todo - 이를 해결하지 위해서는?

            if(!canRead) {
                this.content = null;
            }
        }
    }

    // 게시글 수정 화면 응답 DTO
    public static class UpdateFormDto {
        private Long id;
        private String title;
        private String content;
        private boolean premium;

        public UpdateFormDto(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
            this.premium = board.isPremium();
        }
    }
}
