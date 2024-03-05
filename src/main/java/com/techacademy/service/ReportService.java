package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;

import io.micrometer.common.util.StringUtils;

import org.springframework.transaction.annotation.Transactional;


@Service
public class ReportService {

    private final EmployeeRepository employeeRepository;
    private final ReportRepository reportRepository;
    
    @Autowired
    public ReportService(EmployeeRepository employeeRepository, ReportRepository reportRepository) {
        this.employeeRepository = employeeRepository;
        this.reportRepository = reportRepository;
    }
    
    // [日報] 保存
    @Transactional
    public ErrorKinds save(Report report) {
        
        System.out.println("■■■■■■■■■■ model POST save");
        System.out.println(report);
        
        // 文字数制限のチェック
        reportTextSizeCheck(report);
        
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
        
//        // DBから従業員情報を取得
//        Employee existingReport = findByCode(report.getCode());
//        if (existingReport == null) {
//            return ErrorKinds.INPUT_ERROR;
//        }

//        // 名前が入力されていない場合はエラーを返す
//        if (StringUtils.isEmpty(report.getName())) {
//            return ErrorKinds.INPUT_ERROR;
//        }

//        // パスワードチェック
//        ErrorKinds result = employeePasswordCheck(employee);
//        if (ErrorKinds.CHECK_OK != result) {
//            return ErrorKinds.INPUT_ERROR;
//        }

        // 画面から入力した内容で更新
//        existingReport.setName(report.getName());
//        existingReport.setPassword(report.getPassword());
//        existingReport.setRole(report.getRole());
        
        // 更新日時を更新
//        existingReport.setUpdatedAt(LocalDateTime.now());

        // 保存
//        reportRepository.save(existingReport);

        return ErrorKinds.SUCCESS;
    }

    // [日報] 削除
    @Transactional
    public ErrorKinds delete(String id, UserDetail userDetail) {
        
        Report report = findByReportId(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        
        // 論理削除
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    // [日報] 一覧表示処理
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // [日報] 1件を検索
    public Report findByReportId(String id) {
        
        // findByIdで検索
        Optional<Report> option = reportRepository.findById(id);
        
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        
        return report;
    }

    // employee_codeを取得
    public String getEmployeeCode(String id) {
        
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

    // [日報] 文字数制限のチェック
    public ErrorKinds reportTextSizeCheck(Report report) {
        
        // タイトル：100文字以下
        if (report.getTitle() != null && report.getTitle().length() > 100) {
            return ErrorKinds.TITLE_LENGTH_ERROR;
        }
        
        // 本文: 600文字以下
        if (report.getContent() != null && report.getContent().length() > 600) {
            return ErrorKinds.CONTENT_LENGTH_ERROR;
        }
        
        return ErrorKinds.CHECK_OK;
    }

}
