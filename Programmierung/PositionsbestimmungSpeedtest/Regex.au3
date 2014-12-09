#include <File.au3>

$file = "C:/Users/Ferdinand/Desktop/Staaten_raw.html"
FileOpen($file, 0)

For $i = 1 to _FileCountLines($file)
   $line = FileReadLine($file, $i)
   $aResult = StringRegExp($line, '\"x[2|6] b\d(\st\d)?\"\>([\s\w/üהצ()' & "'" & '-.]{1,40})\<\/div\><div class\=\"x\d b\d(\st\d)?\"\>([-+]?[0-9]*\.?[0-9]*)\s\/\s([-+]?[0-9]*\.?[0-9]*)\<\/div\>', 3)
   $sCity = $aResult[1]
   $sLatitude = $aResult[3]
   $sLongitude = $aResult[4]
   ;ConsoleWrite("Stadt: " & $sCity & ". Latitude: " & $sLatitude & ". Longitude: " & $sLongitude & @CRLF)
   ;locations.add(new Location(0.0, 0.0));
   ConsoleWrite("locations.add(new Location(" & $sLatitude & ", " & $sLongitude & ")); // " & $sCity & @CRLF)
Next
FileClose($file)