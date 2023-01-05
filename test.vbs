Dim oApp

Dim objShell
Set objShell = Wscript.CreateObject("WScript.shell")
curDir = objShell.CurrentDirectory
'msgbox curDir

Set oApp = CreateObject("Excel.Application")

'  Excelは非表示にする
'oApp.Visible = False


'引数のチェック、ファイルを開く
Set WshArguments = WScript.Arguments
Set WshNamed = WshArguments.Named

If WshNamed.Exists("file") Then
	file = curDir & "\" & WshNamed("file")
   	oApp.Workbooks.Open file 'ファイルを開く
   	'outfname =  WshNamed("outfname")
   	'msgbox outfname
   	'msg = WshNamed("msg")
   	'msgbox msg
   	oApp.Run "run" , WshNamed("outfname"), WshNamed("msg")		'outfname / msg: マクロへ渡す引数値
End If
