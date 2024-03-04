package com.techacademy.constants;

// エラーメッセージ定義
public enum ErrorKinds {

    // エラー内容
    // 空白チェックエラー
    BLANK_ERROR,
    // 半角英数字チェックエラー
    HALFSIZE_ERROR,
    // 桁数(8桁~16桁以外)チェックエラー
    RANGECHECK_ERROR,
    // 重複チェックエラー(例外あり)
    DUPLICATE_EXCEPTION_ERROR,
    // 重複チェックエラー(例外なし)
    DUPLICATE_ERROR,
    // ログイン中削除チェックエラー
    LOGINCHECK_ERROR,
    // 日付チェックエラー
    DATECHECK_ERROR,
    // チェックOK
    CHECK_OK,
    // 正常終了
    SUCCESS,

    // 追加されたエラーコード
    // パスワードの桁数チェックエラー
    PASSWORD_LENGTH_ERROR,
    // パスワードの半角英数字チェックエラー
    PASSWORD_HALFSIZE_ERROR,
    // 入力エラー
    INPUT_ERROR,
    // 新しく追加されたエラーコード
    PASSWORD_FORMAT_ERROR,
    // [日報] タイトル文字数エラー 
    TITLE_LENGTH_ERROR,
    // [日報] 内容文字数エラー
    CONTENT_LENGTH_ERROR
    
}
