package com.tenco.spring_blog.purchase;

import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter //Setter이 들어가선 안되기에
@NoArgsConstructor
@Entity
@Table(name = "purchase_tb",
        uniqueConstraints = {@UniqueConstraint(name = "uk_user_board", columnNames = {"user_id", "board_id"})})
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    // 구매 방식을 지불한 포인트
    private int price;

    @CreationTimestamp
    private Timestamp createdAt;

    private LocalDateTime expiredAt;

    @Builder
    public Purchase(User user, Board board, int price, LocalDateTime expiredAt) {
        this.user = user;
        this.board = board;
        this.price = price;
        this.expiredAt = expiredAt;
    }

    // todo - 현재 시점에 열람 가능한지 검증하는 메서드
    public boolean isAccessible(LocalDateTime now) {
        return now.isBefore(this.expiredAt);
    }
}
