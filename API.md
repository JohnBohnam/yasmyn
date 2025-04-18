# Opis API
API REST.

co chcemy obsługiwać (so far):
 - logowanie/rejestracja
 - odczyt tematu na dziś
 - upload zdjęć
 - pobieranie zdjęć

## Autoryzacja

### Rejestracja

#### POST `/auth/register`
Body:
```
{
  "email": "user@example.com",
  "username": "Totręba",
  "password": "securePassword123"
}
```
Response: 
- 201 Created
- 400 Bad Request

### Logowanie

#### POST `/auth/login`

Body:
```
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```
Response:
```
{
  "token": "FIREBASE_AUTH_TOKEN"
}
```
 - 200 OK
 - 401 Unauthorized

## Tematy

### Temat dnia

#### GET `/topic/today`

Response:
```
{
  "date": "2025-04-18",
  "title": "Zrób fikołka",
  "topicId": "2025-04-18",
  "expiresAt": "2025-04-18T23:59:59Z",
}
```

## Użytkownicy

### Info o zalogowanym użytkowniku

#### GET `/user/me`

Headers:
```
Authorization: Bearer FIREBASE_AUTH_TOKEN
```
Response:
```
{
    "userId": "user_1",
    "username": "Jason Piotrulo",
    "email": "pi0trPu55yD3stroyer@gmail.com",
    "profilePhotoUrl": "https://example.com/photo.jpg",
}
```


## Zdjęcia

### Wrzucanie zdjęć (po prostu, bez tematu)

Wrzucanie zdjęć bezpośrednio do firebase:
https://firebase.google.com/docs/storage/web/upload-files#web_2

Zdjęcia wrzucane do kolecji: `/{userId}/photos/{photo_id}`
Pytanie jak generować photo id? Opcje:
1. timestamp + userId (prostsze)
2. Firebase generuje id

Wymagana autentykacja (do skonfigurowania w firebase).

Nie idzie przez backend napisany przez nas.
W odpowiedzi jesr url do zdjęcia.

### Wrzucanie zdjęć (na daily temat)

Dzieje się w dwóch krokach:
 1. upload zdjęcia na firebase
 2. enpoint na backend

#### POST `/photos/daily`

Headers:
```
Authorization: Bearer FIREBASE_AUTH_TOKEN
```
Body:
```
{
    "topicId": "2025-04-18",
    "photoUrl": "https://example.com/photo.jpg",
    "description"?: "Opis czy coś"
}
```
Response:
```
{
    "photoId": "photo_1"
}
```
 - 200 OK
 - 401 Unauthorized

### Pobieranie zdjęć (feed)

Headers:
```
Authorization: Bearer FIREBASE_AUTH_TOKEN
```

#### GET `/photos/feed?limit={limit}&after={offset}`
lub bez offsetu
#### GET `/photos/feed?limit={limit}`
Response:
```
{
  "photos": [
    {
      "id": "photo_1",
      "topicId": "2025-04-18",
      "user": {
        "userId": "user_1",
        "username": "Jason Piotrulo"
        "profilePhotoUrl": "https://example.com/photo.jpg",
      },
      "photoUrl": "https://example.com/photo.jpg",
      "description": "Opis zdjęcia",
    },
    ...
  ],
  "hasMore": true,
  "nextCursor": "abc123"
}
```
 - 200 OK
 - 401 Unauthorized




