import os
import numpy as np
import cv2
import tensorflow as tf
from tensorflow.keras.models import Sequential, load_model
from tensorflow.keras.layers import Conv2D, MaxPooling2D, Flatten, Dense, Dropout
from tensorflow.keras.utils import to_categorical
from sklearn.model_selection import train_test_split


# Функция для загрузки датасета с одной меткой на изображение.
def load_dataset_single_label(image_dir, annot_dir, img_size=(128, 128)):
    images = []
    labels = []  # Здесь будет целое число, соответствующее классу
    label_set = set()
    # Получаем список файлов изображений.
    image_files = os.listdir(image_dir)
    image_files = [f for f in image_files if f.lower().endswith(('.jpg', '.jpeg', '.png'))]
    for img_file in image_files:
        img_path = os.path.join(image_dir, img_file)
        # Подбираем имя файла аннотации (расширение .txt)
        annot_path = os.path.join(annot_dir,
                                  img_file.replace('.jpg', '.txt').replace('.jpeg', '.txt').replace('.png', '.txt'))
        if not os.path.exists(annot_path):
            continue
        # Читаем аннотацию
        with open(annot_path, 'r') as f:
            lines = f.readlines()
        # Извлекаем метки, если их несколько, выбираем первую
        labels_in_img = [line.strip().split()[1] for line in lines if len(line.strip().split()) >= 2]
        if not labels_in_img:
            continue
        # Выбираем первую метку как классифицирующую
        label = labels_in_img[0]
        label_set.add(label)
        # Чтение изображения и его предобработка.
        img = cv2.imread(img_path)
        if img is None:
            continue
        img = cv2.resize(img, img_size)
        img = img.astype('float32') / 255.0
        images.append(img)
        labels.append(label)
    # Создаем упорядоченный список классов
    classes = sorted(list(label_set))
    print("Найденные классы:", classes)
    # Преобразуем строковые метки в числовые
    label_to_index = {label: idx for idx, label in enumerate(classes)}
    numeric_labels = [label_to_index[label] for label in labels]
    # Преобразуем метки в one-hot векторы
    one_hot_labels = to_categorical(numeric_labels, num_classes=len(classes))
    return np.array(images), np.array(one_hot_labels), classes


# Задаем пути к обучающей выборке
train_image_dir = 'HiXray_picture/train/train_image'
train_annot_dir = 'HiXray_picture/train/train_annotation'
# Загрузка тренировочных данных (одна метка на изображение)
images, labels, classes = load_dataset_single_label(train_image_dir, train_annot_dir, img_size=(128, 128))
print("Найдено изображений:", len(images))
if len(images) == 0:
    raise ValueError("Не найдено изображений. Проверьте пути к данным и формат аннотаций.")
# Разбиваем датасет на обучающий и валидационный набор
X_train, X_val, y_train, y_val = train_test_split(images, labels, test_size=0.2, random_state=42)
num_classes = len(classes)
model = Sequential([
    Conv2D(32, (3, 3), activation='relu', input_shape=(128, 128, 3)),
    MaxPooling2D((2, 2)),
    Conv2D(64, (3, 3), activation='relu'),
    MaxPooling2D((2, 2)),
    Flatten(),
    Dense(64, activation='relu'),
    Dropout(0.5),
    Dense(num_classes, activation='softmax')  # softmax для single-label задачи
])
model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
model.summary()
# Обучение модели
history = model.fit(X_train, y_train,
                    epochs=10,
                    batch_size=32,
                    validation_data=(X_val, y_val))
# Сохранение обученной модели
model.save('model.h5')
print("Модель сохранена в файле model.h5")


# Функция предсказания для одного изображения (возвращает единственную предсказанную метку)
def predict_image(model, img_path, classes, img_size=(128, 128)):
    img = cv2.imread(img_path)
    if img is None:
        print("Не удалось загрузить изображение:", img_path)
        return None
    img = cv2.resize(img, img_size)
    img = img.astype('float32') / 255.0
    img = np.expand_dims(img, axis=0)
    preds = model.predict(img)[0]  # получаем вектор вероятностей для каждого класса
    predicted_index = int(np.argmax(preds))
    return classes[predicted_index]


# Пример использования функции предсказания на тестовой выборке
test_image_dir = 'HiXray_picture/test/test_image'
test_annot_dir = 'HiXray_picture/test/test_annotation'
test_files = os.listdir(test_image_dir)
test_files = [f for f in test_files if f.lower().endswith(('.jpg', '.jpeg', '.png'))]
total_count = 0
correct_count = 0
for filename in test_files:
    img_path = os.path.join(test_image_dir, filename)
    annot_filename = filename.replace('.jpg', '.txt').replace('.jpeg', '.txt').replace('.png', '.txt')
    annot_path = os.path.join(test_annot_dir, annot_filename)
    if not os.path.exists(annot_path):
        print(f"Аннотация для {filename} не найдена, пропускаем...")
        continue

    # Читаем аннотацию и выбираем первую метку как истинную
    with open(annot_path, 'r') as f:
        lines = f.readlines()
    true_labels = [line.strip().split()[1] for line in lines if len(line.strip().split()) >= 2]
    if not true_labels:
        continue
    true_label = true_labels[0]

    predicted_label = predict_image(model, img_path, classes, img_size=(128, 128))

    total_count += 1
    if predicted_label == true_label:
        correct_count += 1
        result = "верно"
    else:
        result = "неверно"

    print(f"Изображение: {filename} | Истинная: {true_label} | Предсказанная: {predicted_label} -> {result}")
if total_count > 0:
    accuracy = (correct_count / total_count) * 100
    error_rate = 100 - accuracy
    print("\nОбщее количество протестированных изображений:", total_count)
    print("Точность (accuracy): {:.2f}%".format(accuracy))
    print("Ошибка (error rate): {:.2f}%".format(error_rate))
else:
    print("Подходящих тестовых изображений не найдено.")
