package com.example.quiz.server;

import com.example.quiz.common.Question;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    private List<Question> allQuestions = new ArrayList<>();
    private List<Question> currentGameQuestions = new ArrayList<>();

    private Map<String, Player> players = new ConcurrentHashMap<>();
    private Set<String> answersInRound = new HashSet<>();
    private int currentQuestionIndex = -1;
    private boolean isGameStarted = false;
    private Thread timerThread;

    public GameManager() {
        loadAllQuestions();
    }

    private void loadAllQuestions() {
        allQuestions.add(new Question("Co jest największym wrogiem kowboja?", new String[]{"Indianie", "Szeryf", "Otarcia od siodła", "Brak Wi-Fi"}, 2));
        allQuestions.add(new Question("Ile koni mechanicznych ma jeden prawdziwy koń?", new String[]{"Dokładnie jednego", "Sto", "Około 15 (serio!)", "Zero, bo to zwierzę"}, 2));
        allQuestions.add(new Question("Jak nazywa się koń Chudego z 'Toy Story'?", new String[]{"Płotka", "Mustang", "Bouli", "Chudy"}, 2));
        allQuestions.add(new Question("Czego koń fizycznie NIE potrafi zrobić?", new String[]{"Sikać", "Spać", "Wymiotować", "Mrugać"}, 2));
        allQuestions.add(new Question("Po co w dżinsach kowboja była ta mała kieszonka?", new String[]{"Na samorodek złota", "Na zegarek kieszonkowy", "Na drobniaki", "Na nabój ostatniej szansy"}, 1));
        allQuestions.add(new Question("Co zajmuje najwięcej miejsca w głowie konia?", new String[]{"Mózg", "Zęby", "Oczy", "Pustka"}, 1));
        allQuestions.add(new Question("Jak nazywa się ten krzak, co się turla po pustyni w filmach?", new String[]{"Turlaj-trawa", "Biegacz stepowy", "Kulisty Krzak Zagłady", "Chwast wędrowniczek"}, 1));
        allQuestions.add(new Question("Co to jest centaur?", new String[]{"Pół-człowiek, pół-koń", "Koń z dwoma głowami", "Kowboj na rowerze", "Marka piwa"}, 0));
        allQuestions.add(new Question("Co to jest hobby horse?", new String[]{"Koń hobbysta", "Skakanie z kijem udającym konia", "Gra na konsolę", "Koń wyścigowy na emeryturze"}, 1));
        allQuestions.add(new Question("Z której strony tradycyjnie wsiada się na konia?", new String[]{"Z lewej", "Z prawej", "Od tyłu (przez ogon)", "Z rozpędu"}, 0));
        allQuestions.add(new Question("Do czego głównie służy koniowi ogon?", new String[]{"Do sterowania w zakrętach", "Do odganiania much", "Do zaczepiania klaczy", "Jako hamulec aerodynamiczny"}, 1));
        allQuestions.add(new Question("Co groziło za kradzież konia na Dzikim Zachodzie?", new String[]{"Mandat 50 dolarów", "Piesze wycieczki", "Szubienica", "Mycie garów w saloonie"}, 2));
        allQuestions.add(new Question("O czym najczęściej są piosenki country?", new String[]{"O miłości do traktora", "O stracie dziewczyny, psa lub pick-upa", "O fizyce kwantowej", "O podatkach"}, 1));
        allQuestions.add(new Question("Jak ma na imię słynny koń z mema, który stoi na balkonie?", new String[]{"Juan", "Pablo", "Stefan", "Płotka"}, 0));
        allQuestions.add(new Question("Ile kul mieści 6-strzałowy rewolwer w tanim westernie?", new String[]{"Sześć", "Siedem", "Tyle, ile wymaga fabuła", "Zero"}, 2));
        allQuestions.add(new Question("Jakie magiczne kucyki uczą, że 'Przyjaźń to Magia'?", new String[]{"Kucyki Pony (My Little Pony)", "Konie z Doliny Muminków", "Bojowe Konie Ninja", "Teletubisie"}, 0));
        allQuestions.add(new Question("Do czego (oprócz napadów na bank) służyła kowbojowi chusta?", new String[]{"Jako serwetka", "Do ochrony przed kurzem przy zaganianiu bydła", "Do wycierania konia", "Do lansu na dzielni"}, 1));
        allQuestions.add(new Question("Jakim onomatopeicznym słowem opisuje się bieg konia?", new String[]{"Brum brum", "Szur szur", "Patataj", "Ding dong"}, 2));
        allQuestions.add(new Question("Co tradycyjnie krzyczy kowboj, gdy jest podekscytowany?", new String[]{"Yee-haw!", "Skibidi Toilet!", "UwU!", "Sigma boii"}, 0));
        allQuestions.add(new Question("Czego prawnie NIE wolno robić jadąc na koniu w stanie Utah?", new String[]{"Łowić ryb", "Jeść kebaba", "Śpiewać country", "Strzelać w niebo"}, 0));

    }

    public synchronized void addPlayer(String nick, ClientHandler handler) {
        players.put(nick, new Player(nick, handler));
        broadcastRanking();
    }

    public synchronized void removePlayer(String nick) {
        if (nick != null && players.containsKey(nick)) {
            players.remove(nick);
            answersInRound.remove(nick);
            System.out.println("[LOBBY] Gracz " + nick + " opuścił grę.");
            broadcastRanking();

            if (isGameStarted && !players.isEmpty() && answersInRound.size() >= players.size()) {
                System.out.println("[SYSTEM] Wszyscy pozostali odpowiedzieli. Przechodzę dalej.");
                nextQuestion();
            }
        }
    }

    public synchronized void startGame() {
        if (isGameStarted || players.isEmpty()) return;

        List<Question> shuffled = new ArrayList<>(allQuestions);
        Collections.shuffle(shuffled);
        currentGameQuestions = shuffled.subList(0, Math.min(5, shuffled.size()));

        isGameStarted = true;
        currentQuestionIndex = -1;
        System.out.println("[SYSTEM] NOWA GRA ROZPOCZĘTA (Wylosowano 5 pytań)");

        nextQuestion();
    }

    public synchronized void nextQuestion() {
        if (!isGameStarted) return;
        if (timerThread != null) timerThread.interrupt();

        answersInRound.clear();
        currentQuestionIndex++;

        if (currentQuestionIndex < currentGameQuestions.size()) {
            Question q = currentGameQuestions.get(currentQuestionIndex);

            int displayNum = currentQuestionIndex + 1;
            int total = currentGameQuestions.size();
            System.out.println("[RUNDA] Wyświetlono pytanie nr " + displayNum + "/" + total + ": " + q.content);

            String networkQ = "QUESTION:[Pytanie " + displayNum + "/" + total + "] " + q.content + ";" + q.options[0] + ";" + q.options[1] + ";" + q.options[2] + ";" + q.options[3];
            QuizServer.broadcast(networkQ);

            startTimer(currentQuestionIndex);
        } else {
            System.out.println("[KONIEC] Gra zakończona. Wszystkie pytania zadane.");
            QuizServer.broadcast("FINISH:Koniec gry! Sprawdź ranking.");
            isGameStarted = false;
        }
    }

    private void startTimer(int roundId) {
        timerThread = new Thread(() -> {
            try {
                Thread.sleep(20000);
                synchronized (this) {
                    if (isGameStarted && currentQuestionIndex == roundId) {
                        System.out.println("[TIMER] Czas minął dla pytania " + (roundId + 1));
                        nextQuestion();
                    }
                }
            } catch (InterruptedException ignored) {}
        });
        timerThread.start();
    }

    public synchronized void handleAnswer(String nick, int idx) {
        if (!isGameStarted || answersInRound.contains(nick)) return;

        Question q = currentGameQuestions.get(currentQuestionIndex);
        if (idx == q.correctAnswerIndex) {
            players.get(nick).addPoint();
            System.out.println("[PUNKT] Gracz " + nick + " odpowiedział poprawnie.");
        } else {
            System.out.println("[BŁĄD] Gracz " + nick + " odpowiedział błędnie.");
        }

        answersInRound.add(nick);
        broadcastRanking();

        if (answersInRound.size() >= players.size()) {
            System.out.println("[SYSTEM] Wszyscy odpowiedzieli na pytanie " + (currentQuestionIndex + 1));
            nextQuestion();
        }
    }

    public void broadcastRanking() {
        StringBuilder sb = new StringBuilder("SCORE:");
        players.values().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()))
                .forEach(p -> sb.append(p.getNick()).append("=").append(p.getScore()).append(";"));
        QuizServer.broadcast(sb.toString());
    }
}