Dim oApp
Dim objShell
Set objShell = Wscript.CreateObject("WScript.shell")
Set oApp = CreateObject("Excel.Application")
oApp.Visible = False	'Excelは非表示にする

'引数のチェック、ファイルを開く
Set WshArguments = WScript.Arguments
Set WshNamed = WshArguments.Named
If WshNamed.Exists("file") Then
	curDir = objShell.CurrentDirectory
	file = curDir & "\" & WshNamed("file")
   	oApp.Workbooks.Open file 'ファイルを開く
   	msgbox WshNamed("method")
   	msgbox WshNamed("outfname")
   	msgbox WshNamed("msg")
	'method: 実行するSubルーチン outfname / msg: マクロへ渡す引数値
   	oApp.Run WshNamed("method"), WshNamed("outfname"), WshNamed("msg")
Else
	Msgbox "ファイルが見つかりません。"
End If
