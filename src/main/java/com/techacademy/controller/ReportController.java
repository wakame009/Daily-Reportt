package com.techacademy.controller;

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

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotEmpty;

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
        
        model.addAttribute("listSize", reportService.findAll().size());
        model.addAttribute("reportList", reportService.findAll());
        model.addAttribute("employeeList", employeeService.findAll());
        
        return "reports/list";
    }
    
    // [日報] 詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable String id, Model model) {
        
        model.addAttribute("report", reportService.findByReportId(id));
        model.addAttribute("employee", employeeService.findByCode(reportService.getEmployeeCode(id)));
        
        return "reports/detail";
    }
    
    // [日報] 新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Report report, Model model) {
        
        // ログイン中の授業員名を取得してモデルに追加
        String loggedInEmployeeName = employeeService.getLoggedInEmployeeName();
        model.addAttribute("employeeName", loggedInEmployeeName);
        
        return "reports/new";
    }
    
    // [日報] 新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, Model model) {
        
//        System.out.println("■■■■■■■■■■ controller POST add");
//        System.out.println(report);
        
        // 空白をチェック
        if ("".equals(report.getTitle()) || ("".equals(report.getContent())) )  {
            // 空白だった場合
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));
            return create(report, model);
        }
        
        // 日報の文字数チェック
        ErrorKinds reportTextSizeCheck = reportService.reportTextSizeCheck(report);
        if (ErrorMessage.contains(reportTextSizeCheck)) {
            model.addAttribute(ErrorMessage.getErrorName(reportTextSizeCheck), ErrorMessage.getErrorValue(reportTextSizeCheck));
            return create(report, model);
        }
        
        // 入力チェック
        if (res.hasErrors()) {
            return create(report, model);
        }
        
        // 日報のユーザーコードを設定
//        report.setEmployeeCode(report.getEmployee().getCode());
        
        // 新規日報の保存処理
        reportService.save(report);

        return "redirect:/reports";
    }

    // [日報] 削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable String id, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        ErrorKinds result = reportService.delete(id, userDetail);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", reportService.findByReportId(id));
            return detail(id, model);
        }

        return "redirect:/reports";
    }
    
    // [日報] 更新画面
    @GetMapping(value = "/{id}/update")
    public String update(@PathVariable String id, Model model) {
        
        model.addAttribute("report", reportService.findByReportId(id));
        model.addAttribute("employee", employeeService.findByCode(reportService.getEmployeeCode(id)));
        
        return "reports/update";
    }
    
    // [日報] 更新処理
    // 後で
//    @PostMapping(value = "/{code}/update")
//    public String updateReport(@PathVariable String code, @Validated Report report, BindingResult result, Model model) {
//        
////        // パスワードが空の場合はDBの値をそのまま使う
////        if ("".equals(employee.getPassword())) {
////            Employee existingEmployee = employeeService.findByCode(code);
////            employee.setPassword(existingEmployee.getPassword());
////        }
////        
////        // パスワード仕様のチェック
////        ErrorKinds employeePasswordCheck = employeeService.employeePasswordCheck(employee);
////        if (ErrorMessage.contains(employeePasswordCheck)) {
////            model.addAttribute(ErrorMessage.getErrorName(employeePasswordCheck), ErrorMessage.getErrorValue(employeePasswordCheck));
////            return update(code, model);
////        }
//        
//        // 入力チェック
//        if (result.hasErrors()) {
//            return "reports/update";
//        }        
//        
//        try {
//            
//            // 更新日時を現在日時に設定
//            report.setUpdatedAt(LocalDateTime.now());
//            
//            // 更新処理
//            reportService.update(report);
//            
//        } catch (DataIntegrityViolationException e) {
//            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
//                ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
//            return "reports/update";
//        }
//
//        return "redirect:/reports";
//    }

}
