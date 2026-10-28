package com.tenco.spring_blog.purchase;

import com.tenco.spring_blog._core.util.MyDateUtil;
import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

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

    // 객체가 생성된 시점
    public String getTime() {
        return MyDateUtil.timestampFormat(createdAt);
    }

    @Builder
    public Purchase(User user, Board board, int price) {
        this.user = user;
        this.board = board;
        this.price = price;
    }
}
