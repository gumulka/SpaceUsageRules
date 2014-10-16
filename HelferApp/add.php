<?PHP
@mail("sur@gummu.de","Neue Regel", $_POST["coords"] . "\n" . $_POST["tag"] . "\n" . $_POST["standort"])

?>
