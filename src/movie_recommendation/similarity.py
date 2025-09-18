from sentence_transformers import SentenceTransformer, util
import numpy as np
import sys, json

movies_json = sys.argv[1]
users_json = sys.argv[2]
ratings_json = sys.argv[3]
movies = json.loads(movies_json)
users = json.loads(users_json)
ratings = json.loads(ratings_json)

sentences = [
    f"Genres: {', '.join(m['genres'])} | "
    f"Plot: {m['plot']} | "
    f"Cast: {', '.join(m['cast'])} | "
    f"Director: {m['director']}"
    for m in movies
]

model = SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')
embeddings = model.encode(sentences)

movie_embeddings = model.encode(sentences, normalize_embeddings=True)

movie_id_to_idx = {m["id"]: idx for idx, m in enumerate(movies)}

def build_user_profile(user_id):
    user_vector = np.zeros_like(movie_embeddings[0])
    total_weight = 0

    for rating in ratings:
        if rating["user-id"] == user_id:
            idx = movie_id_to_idx[rating["movie-id"]]
            weight = rating["rating"]
            user_vector += weight * movie_embeddings[idx]
            total_weight += weight
            # print(f"User {user_id} rated movie {rating['movie-id']} with {weight}, adding to profile.")

    if total_weight > 0:
        user_vector /= total_weight
    return user_vector

def recommend_movies(user_id, top_n=3):
    user_vector = build_user_profile(user_id)
    sims = util.cos_sim(user_vector, movie_embeddings)[0]

    rated_movie_ids = {r["movie-id"] for r in ratings if r["user-id"] == user_id}

    if len(rated_movie_ids) < 3:
        return []

    recommendations = []
    for idx in sims.argsort(descending=True):
        movie = movies[idx]
        if movie["id"] not in rated_movie_ids:
            recommendations.append((movie["title"], sims[idx].item()))
        if len(recommendations) >= top_n:
            break

    return recommendations

result = []
for user in users:
    recs = recommend_movies(user["id"], top_n=3)
    if len(recs) != 0:
        # print(f"Recommendations for {user['first_name']} {user['last_name']}:")
        # for title, score in recs:
        #     print(f"   {title} (score={score:.4f})")
        # print()
        result.append({'id': user['id'],
                       'recs': recs})


print(json.dumps({"result": result}))