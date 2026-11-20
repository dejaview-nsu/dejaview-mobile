# Каталог API DejaView для распределения работ

Источник — приложенный Swagger 1.4.1. Последняя колонка сравнивает только точные пути двух спецификаций; «Нет» не означает отсутствие серверной функции. Для фактического кода, аналогов списков и мобильного OIDC использовать [аудит backend](backend-evidence.md). Все пути относительны к `/api/v1`. Наличие пары method/path в public main не доказывает совпадение DTO или работу на стенде. «Этап 1» обозначает целевые операции сокращённого MVP, «Этап 2» — оставшийся MVP PDF, «Этап 3» — функции вне исходного MVP.

| Метод | Путь | operationId | Владелец | UC | Этап | Пара есть в public main |
| --- | --- | --- | --- | --- | --- | --- |
| POST | `/search/image` | `searchByImage` | Dev 2 | UC-07/10 | 1 | Да |
| POST | `/search/video` | `searchByVideo` | Dev 3 | UC-08/10 | 1 | Да |
| POST | `/search/audio` | `searchByAudio` | Dev 4 | UC-09/10 | 1 | Да |
| POST | `/search/text` | `searchByText` | Dev 1 | UC-01 | 1 | Да |
| POST | `/auth/register` | `registerUser` | Dev 4 | UC-03/04 | 2 | Да |
| POST | `/auth/confirm-email` | `confirmEmail` | Dev 4 | UC-03/04 | 2 | Да |
| POST | `/auth/resend-confirmation` | `resendEmailConfirmation` | Dev 4 | UC-03/04 | 2 | Да |
| POST | `/auth/login` | `login` | Dev 4 | UC-04 | 1 | Да |
| POST | `/auth/logout` | `logout` | Dev 4 | UC-16 | 1 | Да |
| GET | `/auth/session` | `getSession` | Dev 4 | UC-04 | 1 | Да |
| GET | `/auth/oidc/{provider}/start` | `startOidcLogin` | Dev 4 | UC-05/19 | 2 | Да |
| GET | `/auth/oidc/{provider}/callback` | `handleOidcCallback` | Backend; Dev 4 интеграция | UC-05/19 | 2 | Да |
| GET | `/auth/oidc/pending` | `getOidcPending` | Dev 4 | UC-05/19 | 2 | Да |
| POST | `/auth/oidc/complete` | `completeOidcRegistration` | Dev 4 | UC-05/19 | 2 | Да |
| POST | `/auth/oidc/exchange` | `exchangeOidcCode` | Dev 4 | UC-05/19 | 2 | Нет |
| POST | `/auth/password-reset/request` | `requestPasswordReset` | Dev 4 | UC-06 | 2 | Да |
| POST | `/auth/password-reset/check` | `checkPasswordResetToken` | Dev 4 | UC-06 | 2 | Да |
| POST | `/auth/password-reset/complete` | `completePasswordReset` | Dev 4 | UC-06 | 2 | Да |
| PUT | `/users/me/password` | `changePassword` | Dev 4 | UC-16 | 2 | Да |
| GET | `/movies/{movie_id}` | `getMovie` | Dev 1 | UC-02 | 1 | Да |
| GET | `/movies/{movie_id}/reviews` | `listMovieReviews` | Dev 1 | UC-02 | 2 | Да |
| POST | `/movies/{movie_id}/reviews` | `createMovieReview` | Dev 2 | UC-13 | 2 | Нет |
| GET | `/reviews/{review_id}` | `getReview` | Dev 2 | UC-13 | 2 | Нет |
| PATCH | `/reviews/{review_id}` | `updateMyReview` | Dev 2 | UC-13 | 2 | Нет |
| DELETE | `/reviews/{review_id}` | `deleteMyReview` | Dev 2 | UC-13 | 2 | Нет |
| PUT | `/reviews/{review_id}/reaction` | `setReviewReaction` | Dev 2 | UC-14 | 2 | Нет |
| DELETE | `/reviews/{review_id}/reaction` | `deleteReviewReaction` | Dev 2 | UC-14 | 2 | Нет |
| GET | `/movies/{movie_id}/rating-summary` | `getMovieRatingSummary` | Dev 1 | UC-02 | 1 | Нет |
| GET | `/movies/{movie_id}/my-state` | `getMyMovieState` | Dev 2 | UC-11/12 | 1 | Нет |
| PUT | `/movies/{movie_id}/my-rating` | `setMyMovieRating` | Dev 2 | UC-13 | 1 | Нет |
| DELETE | `/movies/{movie_id}/my-rating` | `deleteMyMovieRating` | Dev 2 | UC-13 | 1 | Нет |
| GET | `/users/me/watchlist` | `listMyWatchlist` | Dev 2 | UC-11/12 | 1 | Нет |
| PUT | `/users/me/watchlist/{movie_id}` | `addToWatchlist` | Dev 2 | UC-11/12 | 1 | Нет |
| DELETE | `/users/me/watchlist/{movie_id}` | `removeFromWatchlist` | Dev 2 | UC-11/12 | 1 | Нет |
| GET | `/users/me/watched` | `listMyWatched` | Dev 2 | UC-11/12 | 1 | Нет |
| PUT | `/users/me/watched/{movie_id}` | `addToWatched` | Dev 2 | UC-11/12 | 1 | Нет |
| DELETE | `/users/me/watched/{movie_id}` | `removeFromWatched` | Dev 2 | UC-11/12 | 1 | Нет |
| GET | `/users/me` | `getMyProfile` | Dev 3 | UC-15 | 2 | Нет |
| PATCH | `/users/me` | `updateMyProfile` | Dev 3 | UC-15 | 2 | Нет |
| DELETE | `/users/me` | `deleteMyAccount` | Dev 4 | UC-18 | 2 | Нет |
| PUT | `/users/me/avatar` | `uploadMyAvatar` | Dev 2 | UC-20 | 2 | Нет |
| DELETE | `/users/me/avatar` | `deleteMyAvatar` | Dev 2 | UC-20 | 2 | Нет |
| GET | `/users/me/statistics` | `getMyStatistics` | Dev 3 | UC-15 | 2 | Нет |
| GET | `/users/me/ratings` | `listMyRatings` | Dev 3 | UC-15 | 2 | Нет |
| GET | `/users/me/reviews` | `listMyReviews` | Dev 3 | UC-15 | 2 | Нет |
| GET | `/users/me/actions` | `listMyActions` | Dev 3 | UC-15 | 2 | Нет |
| GET | `/billing/plans` | `getBillingPlans` | Dev 1 | UC-17/21/22 | 3 | Нет |
| GET | `/users/me/entitlement` | `getMyEntitlement` | Dev 1 | UC-17/21/22 | 3 | Нет |
| POST | `/billing/checkout` | `createBillingCheckout` | Dev 1 | UC-17/21/22 | 3 | Нет |
| POST | `/billing/subscription/cancel` | `cancelMySubscription` | Dev 1 | UC-17/21/22 | 3 | Нет |
| POST | `/billing/webhook` | `billingWebhook` | Backend | UC-17/21/22 | 3 | Нет |

Всего: 51; этап 1 — 18, этап 2 — 28, этап 3 — 5.

OIDC callback принимает backend от провайдера; Android его не вызывает как обычный Retrofit endpoint. Billing webhook принадлежит серверу. Эти операции сохранены в каталоге, чтобы учесть все 51 метода без ошибочной выдачи серверной работы мобильным разработчикам.

## Операции только в public main

| Метод | Путь | Мобильный владелец адаптера |
| --- | --- | --- |
| GET | `/users/me/lists/{list_type}` | Dev 2 |
| DELETE | `/users/me/lists/{list_type}/{movie_id}` | Dev 2 |
| PUT | `/users/me/lists/{list_type}/{movie_id}` | Dev 2 |
| GET | `/users/me/movies/{movie_id}` | Dev 2 |

## Операции только в приложенном контракте

- `POST /auth/oidc/exchange`
- `POST /billing/checkout`
- `GET /billing/plans`
- `POST /billing/subscription/cancel`
- `POST /billing/webhook`
- `DELETE /movies/{movie_id}/my-rating`
- `PUT /movies/{movie_id}/my-rating`
- `GET /movies/{movie_id}/my-state`
- `GET /movies/{movie_id}/rating-summary`
- `POST /movies/{movie_id}/reviews`
- `DELETE /reviews/{review_id}`
- `GET /reviews/{review_id}`
- `PATCH /reviews/{review_id}`
- `DELETE /reviews/{review_id}/reaction`
- `PUT /reviews/{review_id}/reaction`
- `DELETE /users/me`
- `GET /users/me`
- `PATCH /users/me`
- `GET /users/me/actions`
- `DELETE /users/me/avatar`
- `PUT /users/me/avatar`
- `GET /users/me/entitlement`
- `GET /users/me/ratings`
- `GET /users/me/reviews`
- `GET /users/me/statistics`
- `GET /users/me/watched`
- `DELETE /users/me/watched/{movie_id}`
- `PUT /users/me/watched/{movie_id}`
- `GET /users/me/watchlist`
- `DELETE /users/me/watchlist/{movie_id}`
- `PUT /users/me/watchlist/{movie_id}`
