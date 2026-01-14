# Quiz o koniach i kowbojach
Gra sieciowa typu **klient-serwer** zrealizowana w języku Java. System pozwala na przeprowadzenie turnieju wiedzy dla wielu graczy jednocześnie. Centralny serwer zarządza logiką gry, a klienci łączą się, aby odpowiadać na pytania w tym samym czasie.

## Kluczowe funkcjonalności
1. **Architektura klient-serwer**
   * serwer działa wielowątkowo
   * komunikacja odbywa się tekstowym protokołem własnym przez socket
2. **Mechanika gry**
   * lobby: gracze dołączają i widzą się nawzajem na liście
   * serwer posiada bazę 20 pytań tematycznych, z których losuje się 5 unikalnych pytań na każdą sesję gry
   * gra przechodzi do następnego pytania dopiero gdy wszyscy gracze udzielą odpowiedzi (lub minie czas)
   * na każde pytanie przewidziane jest 20 sekund
3. Ranking
   * po każdej odpowiedzi serwer rozsyła zaktualizowany ranking punktowy do wszystkich podłączonych klientów
  
## Instrukcja uruchomienia


## Przykłady interakcji
1. Logowanie
   - gracz wpisuje nick "szeryf". klient wysyła, a serwer rejestruje gracza
   - klient: JOIN:szeryf
   - serwer: dodaje gracza do lobby
2. Start gry
   - jeden z graczy klika przycisk start.
   - klient: START
   - serwer: losuje 5 pytań z puli i wysyła pierwsze zapytanie do wszystkich
   - serwer -> wszyscy: QUESTION: [Pytanie 1/5]
3. Rozgrywka
   - gracz wybiera odpowiedź b (indeks 1)
   - klient: ANSWER:1
   - serwer: sprawdza poprawność. jeśli ok - dodaje punkt
   - serwer -> wszyscy: SCORE:szeryf=1;bandyta=0;
   - gdy wszyscy odpowiedzą, serwer automatycznie wysyła kolejne pytanie
5. Zakończenie
   -  po 5 pytaniach serwer kończy grę
   - serwer -> wszyscy: FINISH:Koniec gry! Sprawdź ranking

   Projekt wykonany w ramach zaliczenia przedmiotu Programowanie współbieżne i rozproszone.
   


