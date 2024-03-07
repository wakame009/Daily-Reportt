package com.techacademy.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {
    
    private final EmployeeService employeeService;
    private final ReportService reportService;
    
    @Autowired
    public ReportController(EmployeeService employeeService, ReportService reportService) {
        this.employeeService = employeeService;
        this.reportService = reportService;
    }
    
    // [日報] 一覧画面
    @GetMapping
    public String list(Model model) {
        
        // ログイン中の従業員情報を取得してモデルに追加
        Employee loggedInEmployeeInfo = employeeService.getLoggedInEmployeeInfo();
        
        model.addAttribute("listSize", reportService.findReportsByCurrentUser(loggedInEmployeeInfo).size());
        model.addAttribute("reportList", reportService.findReportsByCurrentUser(loggedInEmployeeInfo));
        model.addAttribute("employeeList", employeeService.findAll());
        
        return "reports/list";
    }
    
    // [日報] 詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        
        Report report = reportService.findByReportId(id);
        String currentUsername = principal.getName(); 
        
        // レポートの所有者と現在のユーザーが異なる場合のチェック
        if (!report.getEmployee().getCode().equals(currentUsername) && !employeeService.findByCode(currentUsername).getRole().equals(Employee.Role.ADMIN)) {
            model.addAttribute("error", "アクセス権限がありません");
            return "error";
        }
        
        model.addAttribute("report", reportService.findByReportId(id));
        model.addAttribute("employee", employeeService.findByCode(reportService.getEmployeeCode(id)));
        
        return "reports/detail";
    }
    
    // [日報] 新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Report report, Model model) {
        
        // ログイン中の従業員情報を取得してモデルに追加        
        Employee loggedInEmployeeInfo = employeeService.getLoggedInEmployeeInfo();
        model.addAttribute("employeeInfo", loggedInEmployeeInfo);
        
        // 新規作成する日報オブジェクトを作成し、従業員情報をセット
        report.setEmployee(loggedInEmployeeInfo);
        model.addAttribute("report", report);
        
        return "reports/new";
    }
    
    // [日報] 新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, Model model) {
        
        // 現在ログインしている従業員情報を取得
        Employee loggedInEmployeeInfo = employeeService.getLoggedInEmployeeInfo();
        // 取得した従業員情報をReportオブジェクトにセット
        report.setEmployee(loggedInEmployeeInfo);
        
        if (reportService.isReportDateExists(report.getReportDate(), loggedInEmployeeInfo, null)) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR), ErrorMessage.getErrorValue(ErrorKinds.DATECHECK_ERROR));
            return create(report, model);
        }
        
        // 日報の文字数チェック
        ErrorKinds reportTitleSizeCheck = reportService.reportTitleSizeCheck(report);
        ErrorKinds reportContentSizeCheck = reportService.reportContentSizeCheck(report);
          
        // 文字入力制限チェック
        if ( report.getReportDate() == null || report.getTitle().isEmpty() || report.getContent().isEmpty() || ErrorMessage.contains(reportTitleSizeCheck) || ErrorMessage.contains(reportContentSizeCheck)) {
          
            if (report.getReportDate() == null || report.getTitle().isEmpty() || report.getContent().isEmpty()) {
                model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.REPORT_BLANK_ERROR), ErrorMessage.getErrorValue(ErrorKinds.REPORT_BLANK_ERROR));
            }
            
            if (ErrorMessage.contains(reportTitleSizeCheck)) {
                model.addAttribute(ErrorMessage.getErrorName(reportTitleSizeCheck), ErrorMessage.getErrorValue(reportTitleSizeCheck));
            }
            
            if (ErrorMessage.contains(reportContentSizeCheck)) {
                model.addAttribute(ErrorMessage.getErrorName(reportContentSizeCheck), ErrorMessage.getErrorValue(reportContentSizeCheck));
            }
            
            return create(report, model);
        }
        
        // 入力チェック
        if (res.hasErrors()) {
            return create(report, model);
        }
        
        // 新規日報の保存処理
        reportService.save(report);
        
        return "redirect:/reports";
    }
    
    // [日報] 削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal UserDetail userDetail, Model model, Principal principal) {
        
        ErrorKinds result = reportService.delete(id, userDetail);
        
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", reportService.findByReportId(id));
            return detail(id, model, principal);
        }
        
        return "redirect:/reports";
    }
    
    // [日報] 更新画面
    @GetMapping(value = "/{id}/update")
    public String update(@PathVariable Long id, Model model, Principal principal) {
        
        Report report = reportService.findByReportId(id);
        String currentUsername = principal.getName(); 
        
        // レポートの所有者と現在のユーザーが異なる場合のチェック
        if (!report.getEmployee().getCode().equals(currentUsername) && !employeeService.findByCode(currentUsername).getRole().equals(Employee.Role.ADMIN)) {
            model.addAttribute("error", "アクセス権限がありません");
            return "error";
        }
        
        model.addAttribute("report", reportService.findByReportId(id));
        model.addAttribute("employee", employeeService.findByCode(reportService.getEmployeeCode(id)));
        
        return "reports/update";
    }
    
    // [日報] 更新処理
    @PostMapping(value = "/{id}/update")
    public String updateReport(@PathVariable Long id, @Validated Report report, BindingResult result, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        
        // 権限チェック
        if (!canUpdateReport(id, userDetail.getUsername())) {
            model.addAttribute("error", "この操作には権限がありません。");
            return "error";
        }
        
        // 現在ログインしている従業員情報を取得
        Employee loggedInEmployeeInfo = employeeService.getLoggedInEmployeeInfo();
        // 取得した従業員情報をReportオブジェクトにセット
        report.setEmployee(loggedInEmployeeInfo);
        
        // 更新画面に戻る際には、必ずemployee情報をモデルに追加
        model.addAttribute("employee", loggedInEmployeeInfo);
        
        if (reportService.isReportDateExists(report.getReportDate(), loggedInEmployeeInfo, id)) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR), ErrorMessage.getErrorValue(ErrorKinds.DATECHECK_ERROR));
            
            // 重複があった場合は更新画面に戻る。このとき、入力された値やエラーメッセージを保持
            model.addAttribute("report", report);
            
            return "reports/update";
        }
        
        // 日報の文字数チェック
        ErrorKinds reportTitleSizeCheck = reportService.reportTitleSizeCheck(report);
        ErrorKinds reportContentSizeCheck = reportService.reportContentSizeCheck(report);
        
        // 文字入力制限チェック
        if ( report.getReportDate() == null || report.getTitle().isEmpty() || report.getContent().isEmpty() || ErrorMessage.contains(reportTitleSizeCheck) || ErrorMessage.contains(reportContentSizeCheck)) {
            
            if (report.getReportDate() == null || report.getTitle().isEmpty() || report.getContent().isEmpty()) {
                model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.REPORT_BLANK_ERROR), ErrorMessage.getErrorValue(ErrorKinds.REPORT_BLANK_ERROR));                
            }
            
            if (ErrorMessage.contains(reportTitleSizeCheck)) {
                model.addAttribute(ErrorMessage.getErrorName(reportTitleSizeCheck), ErrorMessage.getErrorValue(reportTitleSizeCheck));
            }
            
            if (ErrorMessage.contains(reportContentSizeCheck)) {
                model.addAttribute(ErrorMessage.getErrorName(reportContentSizeCheck), ErrorMessage.getErrorValue(reportContentSizeCheck));
            }

            // 重複があった場合は更新画面に戻る。このとき、入力された値やエラーメッセージを保持
            model.addAttribute("report", report);
            
            return "reports/update";
        }
        
        // Employee情報付与
        /*   ログインしているユーザーではなく、日報のユーザーであること    */
        model.addAttribute("employee", employeeService.findByCode(reportService.getEmployeeCode(id)));
        report.setEmployee(employeeService.findByCode(reportService.getEmployeeCode(id)));
        
        // 入力チェック
        if (result.hasErrors()) {
            return "reports/update";
        }
        
        try {
            
            // 更新日時を現在日時に設定
            report.setUpdatedAt(LocalDateTime.now());
            
            // 更新処理
            reportService.update(report);
            
        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return "reports/update";
        }

        return "redirect:/reports";
    }
    
    public boolean canUpdateReport(@PathVariable Long id, @PathVariable String code) {
        
        Report report = reportService.findByReportId(id);
        
        if (report == null) {
            return false;
        }
        
        Employee loggedInEmployeeInfo = employeeService.getLoggedInEmployeeInfo();
        
        return loggedInEmployeeInfo.getCode().equals(code) || loggedInEmployeeInfo.getRole() == Employee.Role.ADMIN;
    }

}
