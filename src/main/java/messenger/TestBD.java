package messenger;

import messenger.model.Author;
import messenger.model.Message;
import messenger.model.Talk;
import messenger.model.User;
import messenger.services.TalkService;
import messenger.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

@Component
public class TestBD {
    @Autowired
    private TalkService talkService;

    @Autowired
    private UserService userService;

    private void register() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("login: ");
        String login = scanner.nextLine();
        System.out.println("password: ");
        String password = scanner.nextLine();
        System.out.println("firstName: ");
        String firstName = scanner.nextLine();
        System.out.println("lastName: ");
        String lastName = scanner.nextLine();
        User user = new User(login, "hash(" + password + ")", firstName, lastName, login + "@mail");
        if (!userService.createUser(user)) {
            System.out.println("Логин не уникален!");
            return;
        }
        System.out.println("Успешно");
    }

    private void login() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("login: ");
        String login = scanner.nextLine();
        System.out.println("password: ");
        String password = scanner.nextLine();
        User user = userService.getUserCompletely(login);
        if (user == null || !user.getHashPassword().equals("hash(" + password + ")")) {
            System.out.println("Логин или пароль введен не верно");
            return;
        }
        income(user);
    }

    private void income(User user) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1 - Просмотреть беседы\n" +
                    "2 - Удалить аккаунт\n" +
                    "3 - Выйти");
            int i = scanner.nextInt();
            if (i == 1) {
                talks(user);
            } else if (i == 2) {
                delete(user);
                break;
            } else break;
        }
    }

    private void delete(User user) {
        userService.lockUser(user.getId());
        System.out.println("Удален!");
    }

    private void talks(User user) {
        List<Talk> talks = talkService.getTalks(user.getLogin());
        for (int i = 0; i < talks.size(); i++) {
            System.out.println((i + 1) + " - " + talks.get(i));
        }
        if (talks.size() == 0) System.out.println("Диалогов нет");
        Scanner scanner = new Scanner(System.in);
        System.out.println("1 - Просмотреть диалог\n" +
                "2 - Создать диалог\n" +
                "3 - Удалить диалог\n" +
                "4 - Выйти");
        int i = scanner.nextInt();
        if (i == 1) {
            messages(talks, user);
        } else if (i == 2) {
            createTalk(user);
        } else if (i == 3) {
            deleteTalk(talks);
        }
    }

    private void deleteTalk(List<Talk> talks) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Укажите номер диалога: ");
        int number = scanner.nextInt();
        if (number > talks.size()) {
            System.out.println("Неверно введен номер");
            return;
        }
        Talk talk = talks.get(number - 1);
        talkService.deleteTalk(talk.getId());
    }

    private void createTalk(User user) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Название диалога: ");
        String name = scanner.nextLine();
        Talk talk = talkService.createTalk(name, user.getLogin());
        if (talk == null) {
            System.out.println("У вас уже есть диалог с таким названием");
            return;
        }
        while (true) {
            System.out.println("1 - Добавить участников\n" +
                    "2 - Просмотреть список участников\n" +
                    "3 - Закончить создание диалога\n");
            int i = scanner.nextInt();
            if (i == 1) {
                addUser(talk);
            } else if (i == 2) {
                for (User u : userService.getAllUsers()) {
                    System.out.println(u.getLogin());
                }
            } else break;
        }
    }

    private void addUser(Talk talk) {
        System.out.println("Введите логин участника:\n");
        Scanner scanner = new Scanner(System.in);
        String login = scanner.nextLine();
        if (talkService.addUsersInTalk(talk.getId(), Arrays.asList(login))) {
            System.out.println("Участник добавлен");
            return;
        }
        System.out.println("Не найдено участников с таким логином");
    }

    private void deleteUser(Talk talk) {
        System.out.println("Введите логин участника:\n");
        Scanner scanner = new Scanner(System.in);
        String login = scanner.nextLine();
        if (talkService.deleteUserFromTalk(talk.getId(),login)) {
            System.out.println("Участник удален");
        }
        //System.out.println("Не найдено участников с таким логином");
    }

    private void messages(List<Talk> talks, User user) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Укажите номер диалога: ");
        int number = scanner.nextInt();
        if (number > talks.size()) {
            System.out.println("Неверно введен номер");
            return;
        }
        Talk talk = talks.get(number - 1);
        List<Message> messages = talkService.getMessages(talk.getId());
        for (Message message : messages) {
            System.out.println(message);
        }
        while (true) {
            System.out.println("1 - Написать сообщение\n" +
                    "2 - Добавить участника\n" +
                    "3 - Удалить участника\n" +
                    "4 - Выйти");
            int i = scanner.nextInt();
            if (i == 1) {
                System.out.println("Введите сообщение:");
                String text = (new Scanner(System.in)).nextLine();
                Message message = new Message(text, new Date(),
                        new Author(user.getId(), user.getFirstName(), user.getLastName()));
                talkService.writeMessage(message, talk.getId());
            } else if (i == 2) {
                addUser(talk);
            } else if (i == 3) {
                deleteUser(talk);
            } else break;
        }
    }

    void test() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1 - Зарегистрироваться\n" +
                    "2 - Войти\n" +
                    "3 - Выйти из программы");
            int i = scanner.nextInt();
            if (i == 1) {
                register();
            } else if (i == 2) {
                login();
            } else break;
        }
    }
}
