# Bank Card Management System

## Статус проекта: Выполнен и готов к использованию

Полнофункциональная система управления банковскими картами с безопасной аутентификацией, шифрованием данных и ролевым доступом.

## Оглавление

- [Функциональность](#-функциональность)
- [Технологии](#-технологии)
- [Архитектура](#-архитектура)
- [Установка и запуск](#-установка-и-запуск)
- [API Документация](#-api-документация)
- [База данных](#-база-данных)
- [Безопасность](#-безопасность)
- [Тестирование](#-тестирование)
- [Разработчики](#-разработчики)

## Функциональность

### Пользовательские возможности
-  **Регистрация и аутентификация** (JWT)
-  **Управление картами** - создание, просмотр, блокировка, удаление
-  **Переводы между своими картами**
-  **Переводы другим пользователям** 
-  **Поиск и фильтрация** карт с пагинацией
-  **Безопасный просмотр** с маскированием номеров карт

### Администраторские возможности
-  **Полный контроль** над всеми картами и пользователями
-  **CRUD операции** для пользователей и карт
-  **Управление ролями** пользователей

## Технологии
### Backend
- **Java 17+** - основной язык программирования
- **Spring Boot 3.5.0** - фреймворк
- **Spring Security** - аутентификация и авторизация
- **Spring Data JPA** - работа с базой данных
- **JWT** - токены для безопасной аутентификации

### База данных
- **PostgreSQL 15** - основная СУБД
- **Liquibase** - управление миграциями базы данных
- **Hibernate** - ORM

### Безопасность
- **JWT** с секретным ключом
- **BCrypt** для хеширования паролей
- **AES шифрование** для номеров карт и CVV
- **Ролевая модель** доступа (USER/ADMIN)

### Документация и тестирование
- **OpenAPI 3.0 / Swagger** - документация API
- **JUnit 5 + Mockito** - модульное тестирование

## Архитектура
```
bank-card-management/
├──  src/main/java/com/example/bankcards/
│   ├──  config/          # Конфигурации Spring
│   ├──  controller/      # REST контроллеры
│   ├──  entity/          # Сущности JPA
│   ├──  repository/      # Spring Data репозитории
│   ├──  service/         # Бизнес-логика
│   ├──  security/        # JWT и безопасность
│   ├──  dto/            # Data Transfer Objects
│   └──  util/           # Утилиты (шифрование, валидация)
├──  src/main/resources/
│   ├──  db/changelog/   # Миграции Liquibase
│   └── application.yml    # Конфигурация приложения
└──  src/test/java/      # Модульные тесты
```

## Установка и запуск

### Предварительные требования
- Java 17 или выше
- Maven 3.6+
- PostgreSQL 15+
- Docker (опционально)

### Способ 1: Локальная установка

1. **Клонирование репозитория**
```bash
git clone https://github.com/your-username/bank-card-management.git
cd bank-card-management
```

2. **Настройка базы данных**
```sql
CREATE DATABASE bank_cards_db;
CREATE USER bank_user WITH PASSWORD 'bank_password';
GRANT ALL PRIVILEGES ON DATABASE bank_cards_db TO bank_user;
```

3. **Настройка приложения**
Создайте `application.yml` в `src/main/resources/`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bank_cards_db
    username: bank_user
    password: bank_password

jwt_secret: your-super-secret-jwt-key-here
app:
  encryption:
    secret: your-encryption-key-for-cards
```

4. **Запуск приложения**
```bash
mvn clean install
mvn spring-boot:run
```

### Способ 2: Docker (рекомендуется)

```bash
# Запуск всего стека
docker-compose up --build

# Или поэтапно
docker-compose up postgres -d
mvn clean package
docker build -t bank-cards-app .
docker run -p 8080:8080 bank-cards-app
```

## API Документация

После запуска приложения документация доступна по адресам:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## База данных

### Схема базы данных
```sql
users (id, username, email, password, role, created_at)
bank_cards (id, card_number_encrypted, card_holder, expiration_date, 
            cvv_encrypted, balance, status, user_id, created_at, updated_at)
transactions (id, from_card_id, to_card_id, amount, 
              transaction_date, status, description)
```

### Миграции
Миграции управляются через Liquibase:
```bash
# Просмотр выполненных миграций
mvn liquibase:history

# Применение новых миграций
mvn liquibase:update
```

## Безопасность

### Шифрование данных
- **Номера карт** и **CVV** шифруются AES-256 перед сохранением
- **Пароли** хешируются BCrypt
- **JWT токены** с ограниченным временем жизни

### Ролевая модель
- **ROLE_USER** - управление своими картами, переводы между своими картами
- **ROLE_ADMIN** - полный доступ ко всем данным системы


## Тестирование
### Запуск тестов
```bash
# Все тесты
mvn test

# Только unit-тесты
mvn test -Dtest="*Test"

# С отчетом о покрытии
mvn jacoco:report
```

## Разработчик
- **Вигерин Данил**

---

**⭐ Не забудьте поставить звезду репозиторию, если проект вам понравился!**

---

*Последнее обновление: Ноябрь 2025*
