package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;

import org.springframework.transaction.annotation.Transactional;


@Service
public class ReportService {

    private final ReportRepository reportRepository;
    
    @Autowired
    public ReportService(EmployeeRepository employeeRepository, ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }
    
    // [日報] 新規登録
    @Transactional
    public ErrorKinds save(Report report) {
        
        // 論理削除のデフォルト値を設定
        report.setDeleteFlg(false);
        
        // 登録日時と更新日時を設定
        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        
        // レポートを保存
        reportRepository.save(report);
        
        return ErrorKinds.SUCCESS;
        
    }
    
    // [日報] 更新
    @Transactional
    public ErrorKinds update(Report report) {
        
        // DBから既存の日報情報を取得
        Report existingReport = findByReportId(report.getId());
        if (existingReport == null) {
            return ErrorKinds.INPUT_ERROR;
        }
        
        // 画面から入力値で更新
        existingReport.setReportDate(report.getReportDate());
        existingReport.setTitle(report.getTitle());
        existingReport.setContent(report.getContent());
        
        // 更新日時を更新
        existingReport.setUpdatedAt(LocalDateTime.now());
        
        // 保存
        reportRepository.save(existingReport);

        return ErrorKinds.SUCCESS;
    }

    // [日報] 削除
    @Transactional
    public ErrorKinds delete(Long id, UserDetail userDetail) {
        
        Report report = findByReportId(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        
        // 論理削除
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }
    
    // 【日報】日報へのアクセス制御
    public List<Report> findReportsByCurrentUser(Employee currentUser) {
        if (currentUser.getRole() == Employee.Role.ADMIN) {
            // 管理者権限の場合、すべての日報を返却
            return reportRepository.findAll();
        } else {
            // 一般権限の場合、自分の日報のみ返却
            return reportRepository.findByEmployee(currentUser);
        }
    }

    // [日報] 1件を検索
    public Report findByReportId(Long id) {
        
        // findByIdで検索
        Optional<Report> option = reportRepository.findById(id);
        
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        
        return report;
    }

    // employee_codeを取得
    public String getEmployeeCode(Long id) {
        
        // findByIdで検索
        Optional<Report> option = reportRepository.findById(id);
        
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        
        // Reportがnullでないことを確認し、関連するEmployeeオブジェクトからemployee_codeを取得する
        if (report != null && report.getEmployee() != null) {
            return report.getEmployee().getCode();
        } else {
            return null;
        }
        
    }

    // タイトル：100文字以下
    public ErrorKinds reportTitleSizeCheck(Report report) {
        
        if (report.getTitle() != null && report.getTitle().length() > 100) {
            return ErrorKinds.TITLE_LENGTH_ERROR;
        }
        
        return ErrorKinds.CHECK_OK;
    }
    
    // 本文: 600文字以下
    public ErrorKinds reportContentSizeCheck(Report report) {
        
        if (report.getContent() != null && report.getContent().length() > 600) {
            return ErrorKinds.CONTENT_LENGTH_ERROR;
        }
        
        return ErrorKinds.CHECK_OK;
    }

}
