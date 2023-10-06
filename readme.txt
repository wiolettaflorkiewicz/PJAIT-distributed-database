Program DatabaseNode to rozproszony system baz danych, 
który umożliwia wielu węzłom przechowywanie i pobieranie 
par klucz-wartość. Każdy węzeł przechowuje określoną parę 
klucz-wartość i może komunikować się z innymi węzłami w sieci 
w celu pobierania lub aktualizowania wartości. Komunikacja 
między węzłami opiera się na modelu klient-serwer, w którym 
każdy węzeł działa zarówno jako klient, jak i serwer.

Sieć jest zorganizowana jako zbiór węzłów, które mogą się ze 
sobą łączyć i komunikować. Każdy węzeł ma przypisany unikalny 
numer portu i adres IP, a węzły można dodawać do sieci, podając 
ich adres IP i numer portu jako argumenty wiersza poleceń. 
Węzły można również łączyć z innymi węzłami, określając adresy IP 
i numery portów węzłów, z którymi mają się łączyć, jako 
argumenty wiersza poleceń.

Protokół komunikacyjny zastosowany w tym programie opiera się 
na przesyłaniu komunikatów w postaci ciągów znaków przez gniazda 
TCP. Komunikaty przesyłane między węzłami zawierają polecenia 
lub operacje, które muszą wykonać węzły. Polecenia są sformatowane 
jako łańcuchy znaków w następującym formacie: „polecenie arg1 arg2 arg3…”.

Polecenia, które można przesyłać między węzłami to:

     "set-value key:value" - ustawia parę klucz-wartość węzła na podany klucz i wartość
     "get-value key" - pobiera wartość skojarzoną z podanym kluczem z węzła
     "find-key value" - pobiera klucz powiązany z podaną wartością z węzła
     "get-max" - pobiera parę klucz-wartość z najwyższym kluczem z węzła
     „get-min” — pobiera parę klucz-wartość z najniższym kluczem z węzła
     "get-host" - pobiera adres węzła
     "add-connection address" - dodaje podany adres do listy połączeń węzła
     "remove-connection address" - usuwa podany adres z listy połączeń węzła

Gdy węzeł otrzyma polecenie, wykonuje odpowiednią operację i odsyła 
wynik w postaci ciągu znaków. Na przykład, jeśli węzeł otrzyma 
polecenie „get-value 5”, zwróci wartość powiązaną z kluczem 5 w postaci ciągu znaków.

W systemie zaimplementowano również mechanizm obsługi wielu klientów 
jednocześnie. Gdy klient łączy się z węzłem, węzeł tworzy nowy wątek 
do obsługi żądania. Dzięki temu węzeł może obsługiwać wielu klientów 
jednocześnie bez blokowania żądań innych klientów.

Aby uruchomić program, użytkownik musi podać argumenty wiersza poleceń 
określające numer portu, parę klucz-wartość oraz adresy węzłów, z którymi 
ma się połączyć. Po uruchomieniu programu użytkownik może połączyć się z 
węzłem za pomocą programu klienckiego i wysyłać polecenia w celu pobrania 
lub zaktualizowania wartości w bazie danych.

Podsumowując, program DatabaseNode to rozproszony system baz danych, który 
umożliwia wielu węzłom przechowywanie i pobieranie par klucz-wartość. Węzły 
komunikują się ze sobą za pomocą prostego i wydajnego protokołu komunikacyjnego 
opartego na gniazdach i ciągach znaków TCP. System pozwala również na obsługę 
wielu klientów jednocześnie za pomocą wątków. Program można uruchomić, podając 
argumenty wiersza poleceń określające numer portu, parę klucz-wartość oraz adresy 
węzłów, z którymi należy się połączyć.