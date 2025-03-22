<h1><b>Техническое решение</b></h1>
1.<b></b>Архитектура решения.</b>

  Диаграмма решения:(https://github.com/Omazz/S7-hackathon/blob/main/Tech/%D1%84%D0%BE%D1%82%D0%BE.png)
  <h1>Используемые технологии:</h1>

  <h2>Языки программирования:</h2>
  
  Java (основной backend и frontend)
  Python (ML-модель)
  
  <h2>Фреймворки и библиотеки:</h2>
  
  Spring Framework (backend)
  JavaFX (пользовательский интерфейс)
  Hibernate (ORM)
  TensorFlow/Keras (ML-модель)
  OpenCV (обработка изображений)
  NumPy (работа с массивами данных)
  Инфраструктура:
  
  Apache Kafka (брокер сообщений для асинхронного взаимодействия)
  PostgreSQL (хранение метаданных и результатов)
  <h2>Данные:</h2>
  
  Датасет HiXray для обучения модели
  Источник: https://github.com/hixray-author/hixray
  Предобработка: масштабирование изображений до 128x128, нормализация
  <h2>ML модель:</h2>
  
  Архитектура: Сверточная нейронная сеть (CNN)
  Слои: Conv2D + MaxPooling2D + Dense
  Метрики: accuracy на валидационной выборке
  Обучение: 10 эпох, batch size 32
  
  <h2>Потенциал масштабирования:</h2>
  
  Горизонтальное масштабирование через добавление ML-воркеров
  Вертикальное масштабирование через увеличение вычислительных ресурсов
  Возможность распределенного обучения на multiple GPU
  Масштабирование Kafka для увеличения пропускной способности
  <h2>Замоканные компоненты:</h2>
  
  Некоторые функции реального времени (будут реализованы через Kafka Streams)
  Полноценная система логирования (планируется ELK Stack)
  Мониторинг производительности (планируется Prometheus + Grafana)
2.<h1>Инструкция по развертыванию </h1>

Требования к системе:
- CPU: минимум 4 ядра
- RAM: минимум 16 GB
- GPU: NVIDIA с поддержкой CUDA (рекомендуется минимум 6GB VRAM)
- Дисковое пространство: минимум 50 GB

Шаг 1: Установка Docker
```bash
sudo apt-get update
sudo apt-get install docker.io
sudo systemctl start docker
sudo systemctl enable docker
```

Шаг 2: Развертывание PostgreSQL
```bash
docker pull postgres:latest
docker run -d \
    --name postgres \
    -e POSTGRES_PASSWORD=your_password \
    -e POSTGRES_DB=xray_db \
    -p 5432:5432 \
    postgres
```

Шаг 3: Установка и настройка Kafka
```bash
# Загрузка Kafka образа
docker pull confluentinc/cp-kafka:latest
docker pull confluentinc/cp-zookeeper:latest

# Запуск Zookeeper
docker run -d \
    --name zookeeper \
    -p 2181:2181 \
    confluentinc/cp-zookeeper:latest

# Запуск Kafka
docker run -d \
    --name kafka \
    -p 9092:9092 \
    -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
    -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
    confluentinc/cp-kafka:latest
```

Шаг 4: Настройка Python окружения для ML-модели
```bash
# Установка Python зависимостей
pip install tensorflow opencv-python numpy scikit-learn

# Создание виртуального окружения
python -m venv venv
source venv/bin/activate
```

Шаг 5: Установка Java и Maven
```bash
sudo apt-get install openjdk-11-jdk
sudo apt-get install maven
```

Шаг 6: Сборка и запуск Spring приложения
```bash
# Клонирование репозитория
git clone <url-вашего-репозитория>
cd <директория-проекта>

# Сборка проекта
mvn clean install

# Запуск приложения
java -jar target/your-application.jar
```

Шаг 7: Настройка JavaFX клиента
```bash
# Сборка JavaFX приложения
cd frontend
mvn clean package

# Запуск JavaFX приложения
java -jar target/frontend.jar
```

Шаг 8: Проверка работоспособности системы
1. Проверить доступность Kafka:
```bash
kafka-topics.sh --list --bootstrap-server localhost:9092
```

2. Проверить подключение к PostgreSQL:
```bash
psql -h localhost -U postgres -d xray_db
```

3. Проверить работу Spring приложения:
```bash
curl http://localhost:8080/health
```

Шаг 9: Загрузка и подготовка датасета
```bash
# Клонирование репозитория с датасетом
git clone https://github.com/hixray-author/hixray.git

# Создание необходимых директорий
mkdir -p data/{train,test}
```

Шаг 10: Запуск обучения модели
```bash
python train_model.py
```

Мониторинг и поддержка:
1. Логи Kafka: `docker logs kafka`
2. Логи PostgreSQL: `docker logs postgres`
3. Логи Spring приложения: `tail -f logs/application.log`

Рекомендации по масштабированию:
- Для увеличения производительности ML-компонента добавьте GPU
- При большой нагрузке увеличьте количество партиций Kafka
- Настройте репликацию PostgreSQL для отказоустойчивости

Требования к сети:
- Открытые порты: 5432 (PostgreSQL), 9092 (Kafka), 8080 (Spring), 2181 (Zookeeper)
- Стабильное интернет-соединение для загрузки зависимостей

  <h1>Весь код находится в директории Tech(папка Web-service,файл Model,файл JavaFX_part)</h1>
