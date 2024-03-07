package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReportRepository reportRepository;
    
    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, ReportRepository reportRepository) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.reportRepository = reportRepository;
    }

    // 従業員保存
    @Transactional
    public ErrorKinds save(Employee employee) {
        
        // 従業員番号重複チェック
        if (findByCode(employee.getCode()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }
        
        employee.setDeleteFlg(false);
        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);
        
        employeeRepository.save(employee);
        
        return ErrorKinds.SUCCESS;
    }
    
    // 従業員更新
    @Transactional
    public ErrorKinds update(Employee employee) {
        
        // DBから従業員情報を取得
        Employee existingEmployee = findByCode(employee.getCode());
        if (existingEmployee == null) {
            return ErrorKinds.INPUT_ERROR;
        }
        
        // 名前が入力されていない場合はエラーを返す
        if (StringUtils.isEmpty(employee.getName())) {
            return ErrorKinds.INPUT_ERROR;
        }
        
        // 画面から入力した内容で更新
        existingEmployee.setName(employee.getName());
        existingEmployee.setPassword(employee.getPassword());
        existingEmployee.setRole(employee.getRole());
        
        // 更新日時を更新
        existingEmployee.setUpdatedAt(LocalDateTime.now());
        
        // 保存
        employeeRepository.save(existingEmployee);

        return ErrorKinds.SUCCESS;
    }

    // 従業員削除
    @Transactional
    public ErrorKinds delete(String code, UserDetail userDetail) {
        
        // 管理者権限のチェック
        if (!userDetail.getEmployee().getRole().equals(Employee.Role.ADMIN)) {
            return ErrorKinds.INPUT_ERROR;
        }
        
        // 自分を削除しようとした場合はエラーメッセージを表示
        if (code.equals(userDetail.getEmployee().getCode())) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }
        
        // 従業員の存在確認チェック
        Employee employee = findByCode(code);
        if (employee == null) {
            return ErrorKinds.INPUT_ERROR;
        }

        // 該当従業員の日報を論理削除
        List<Report> reports = reportRepository.findByEmployee(employee);
        LocalDateTime now = LocalDateTime.now();
        // 個々の日報を更新
        for (Report report : reports) {
            report.setDeleteFlg(true);
            report.setUpdatedAt(now);
            reportRepository.save(report);
        }

        // 従業員を論理削除
        employee.setDeleteFlg(true);
        employee.setUpdatedAt(now);
        employeeRepository.save(employee);

        return ErrorKinds.SUCCESS;
    }
    
    // 従業員一覧表示処理
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }
    
    // 現在ログインしている従業員情報の返却
    public Employee getLoggedInEmployeeInfo() {
        
        // 現在の従業員の詳細を取得
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // 従業員の識別子を取得
        String code = userDetails.getUsername();
        
        // DBからfindByIdで検索
        Optional<Employee> option = employeeRepository.findById(code);
        
        // 取得できなかった場合はnullを返す
        Employee employee = option.orElse(null);
        
        return employee;
    }
    
    // 現在ログインしている従業員のコードを返却
    public String getLoggedInEmployeeCode() {
        
        // 現在の従業員の詳細を取得
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // 従業員の識別子（コード）を返却
        return userDetails.getUsername();
    }
    
    // 1件を検索
    public Employee findByCode(String code) {
        
        // findByIdで検索
        Optional<Employee> option = employeeRepository.findById(code);
        
        // 取得できなかった場合はnullを返す
        Employee employee = option.orElse(null);
        
        return employee;
    }

    // 従業員パスワードチェック
    public ErrorKinds employeePasswordCheck(Employee employee) {
        
        // 従業員パスワードの半角英数字チェック処理
        if (isHalfSizeCheckError(employee)) {
            return ErrorKinds.HALFSIZE_ERROR;
        }
        
        // 従業員パスワードの8文字～16文字チェック処理
        if (isOutOfRangePassword(employee)) {
            return ErrorKinds.RANGECHECK_ERROR;
        }
        
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        return ErrorKinds.CHECK_OK;
    }

    // 従業員パスワードの半角英数字チェック処理
    private boolean isHalfSizeCheckError(Employee employee) {
        
        // 半角英数字チェック
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(employee.getPassword());
        
        return !matcher.matches();
    }

    // 従業員パスワードの8文字～16文字チェック処理
    // true: 文字数制限が仕様になっていない
    // false: 文字数制限が仕様
    private boolean isOutOfRangePassword(Employee employee) {
        
        // 桁数チェック
        int passwordLength = employee.getPassword().length();
        
        return passwordLength < 8 || 16 < passwordLength;
    }

}
