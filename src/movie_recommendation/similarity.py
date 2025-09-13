from sentence_transformers import SentenceTransformer, util
import numpy as np

movies = [
  {
    "movie_id": 1,
    "title": "Mall Cop",
    "genres": ["Comedy", "Action"],
    "plot": "A bumbling mall security guard must step up to protect shoppers when a heist threatens his mall.", 
    "cast": [
      "Kevin James as Paul Blart",
      "Keir O'Donnell as Uwe",
      "Jayma Mays as Amy",
      "Bobby Cannavale as Vince"
    ],
    "director": "Steve Carr"
  },
  {
    "movie_id": 2,
    "title": "Twister",
    "genres": ["Action", "Adventure", "Disaster"],
    "plot": "A group of storm chasers race against time to launch a new tornado-monitoring device while battling deadly twisters in the Midwest.",
    "cast": [
      "Helen Hunt as Dr. Jo Harding",
      "Bill Paxton as Bill Harding",
      "Philip Seymour Hoffman as Dusty",
      "Cary Elwes as Jonas Miller"
    ],
    "director": "Jan de Bont"
  },
  {
    "movie_id": 3,
    "title": "Jaws",
    "genres": ["Thriller", "Horror", "Adventure"],
    "plot": "A great white shark terrorizes a small beach town, forcing a police chief, a marine biologist, and a professional shark hunter to work together to stop it.", 
    "cast": [
      "Roy Scheider as Chief Martin Brody",
      "Robert Shaw as Quint",
      "Richard Dreyfuss as Matt Hooper",
      "Lorraine Gary as Ellen Brody"
    ],
    "director": "Steven Spielberg"
  },
  {
    "movie_id": 4,
    "title": "Observe and Report",
    "genres": ["Dark Comedy", "Crime"],
    "plot": "A mall security guard with delusions of grandeur attempts to take down a flasher while struggling with his own personal issues.", 
    "cast": [
      "Seth Rogen as Ronnie Barnhardt",
      "Anna Faris as Brandi",
      "Ray Liotta as Detective Harrison",
      "Michael Peña as Dennis"
    ],
    "director": "Jody Hill"
  },
  {
    "movie_id": 5,
    "title": "Pirates of the Caribbean: The Curse of the Black Pearl",
    "genres": ["Action", "Adventure", "Fantasy"],
    "plot": "Blacksmith Will Turner teams up with eccentric pirate Captain Jack Sparrow to save Elizabeth Swann from cursed pirates.",
    "cast": ["Johnny Depp", "Orlando Bloom", "Keira Knightley"],
    "director": "Gore Verbinski"
  }, 
  {
    "movie_id": 6,
    "title": "Sharknado",
    "genres": ["Action", "Comedy", "Horror", "Sci-Fi"],
    "plot": "A freak hurricane causes sharks to be swept up into tornadoes, threatening to destroy Los Angeles and leaving a group of survivors to fight back.",
    "cast": [
      "Ian Ziering as Fin Shepard",
      "Tara Reid as April Wexler",
      "John Heard as George",
      "Cassie Scerbo as Nova"
    ],
    "director": "Anthony C. Ferrante"
  }, 
  {
    "movie_id": 7,
    "title": "Pirates of the Caribbean: At World's End",
    "genres": ["Action", "Adventure", "Fantasy"],
    "plot": "Captain Barbossa, Will Turner and Elizabeth Swann must sail off the edge of the map, navigate treachery and betrayal, to save Jack Sparrow.",
    "cast": ["Johnny Depp", "Orlando Bloom", "Keira Knightley"],
    "director": "Gore Verbinski"
  }
]

users = [
    {"user_id": 1, "name": "Ana"},
    {"user_id": 2, "name": "Betty"},
    {"user_id": 3, "name": "Carlos"},
    {"user_id": 4, "name": "Dana"}
]

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
print(f'Embeddings for {len(movie_embeddings)} movies')

user_ratings = [
    {"user_id": 1, "movie_id": 1, "rating": 3},
    {"user_id": 1, "movie_id": 2, "rating": 1},
    {"user_id": 1, "movie_id": 3, "rating": 1},
    {"user_id": 1, "movie_id": 6, "rating": 1},
    {"user_id": 1, "movie_id": 5, "rating": 5},
    {"user_id": 2, "movie_id": 1, "rating": 1},
    {"user_id": 2, "movie_id": 3, "rating": 4},
    {"user_id": 2, "movie_id": 4, "rating": 1},
    {"user_id": 3, "movie_id": 1, "rating": 3},
    {"user_id": 3, "movie_id": 2, "rating": 1},
    {"user_id": 3, "movie_id": 3, "rating": 1},
    {"user_id": 3, "movie_id": 4, "rating": 1},
    {"user_id": 4, "movie_id": 2, "rating": 3}, 
    {"user_id": 4, "movie_id": 3, "rating": 5},
    {"user_id": 4, "movie_id": 4, "rating": 4},
    {"user_id": 4, "movie_id": 6, "rating": 4}
]

movie_id_to_idx = {m["movie_id"]: idx for idx, m in enumerate(movies)}
# print('Movie id to idx:', movie_id_to_idx)

def build_user_profile(user_id):
    user_vector = np.zeros_like(movie_embeddings[0])
    total_weight = 0

    for rating in user_ratings:
        if rating["user_id"] == user_id:
            idx = movie_id_to_idx[rating["movie_id"]]
            weight = rating["rating"]
            user_vector += weight * movie_embeddings[idx]
            total_weight += weight
            print(f"User {user_id} rated movie {rating['movie_id']} with {weight}, adding to profile.")

    if total_weight > 0:
        user_vector /= total_weight
    return user_vector

def recommend_movies(user_id, top_n=2):
    user_vector = build_user_profile(user_id)
    sims = util.cos_sim(user_vector, movie_embeddings)[0]

    rated_movie_ids = {r["movie_id"] for r in user_ratings if r["user_id"] == user_id}

    recommendations = []
    for idx in sims.argsort(descending=True):
        movie = movies[idx]
        if movie["movie_id"] not in rated_movie_ids:
            recommendations.append((movie["title"], sims[idx].item()))
        if len(recommendations) >= top_n:
            break

    return recommendations

for user in users:
    recs = recommend_movies(user["user_id"], top_n=2)
    print(f"Recommendations for {user['name']}:")
    for title, score in recs:
        print(f"   {title} (score={score:.4f})")
    print()

