call mvn -DskipTests install
IF "%1" == "deploy" (
    psftp turbot@192.168.1.5 -pw kgtnbtrp -b psftp.scr
)
