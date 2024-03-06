
package com.techacademy.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "reports")
@SQLRestriction("delete_flg = false")
public class Report {
    
    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    // 日付
    @Column(columnDefinition="DATE", nullable = false)
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;
    
    // タイトル
    @Column(length = 100, nullable = false)
    @NotEmpty
    private String title;
    
    // 内容
    @Column(columnDefinition="LONGTEXT", nullable = false)
    @NotEmpty
    private String content;
    
    // 社員番号
    @ManyToOne
    @JoinColumn(name = "employee_code", referencedColumnName = "code", nullable = false)
    private Employee employee;
    
    // 社員番号を取得するメソッド
    public String getEmployeeCode() {
        if (employee != null) {
            return employee.getCode();
        } else {
            return null;
        }
    }
    
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    
    // 削除フラグ(論理削除を行うため)
    @Column(columnDefinition="TINYINT", nullable = false)
    private boolean deleteFlg;
    
    // 登録日時
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // 更新日時
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}