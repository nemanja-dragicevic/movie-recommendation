# Movie Recommendation System

This project implements a movie recommendation system using the Alternating Least Squares (ALS) algorithm.
ALS is a matrix factorization technique commonly used in collaborative filtering, where the goal is to predict user preferences for items (e.g., movies) based on observed user–item interactions (e.g., ratings).

The system learns latent user and movie factors from a sparse user–movie rating matrix and uses them to recommend new movies to users.

## ⚙️ How ALS Works

ALS tries to approximate the user–item rating matrix R as the product of two lower-dimensional matrices: R ≈ U × Vᵀ, where:
- R → User–Movie ratings matrix (users × movies)
- U → User latent factor matrix (users × features)
- V → Movie latent factor matrix (movies × features)

Steps:
- Initialization: Start with random user and item feature vectors.
- Fix Movie Matrix (V), solve for User Matrix (U).
- Fix User Matrix (U), solve for Movie Matrix (V).
- Alternate between the two until convergence or max iterations.

The optimization objective is: minimize || R - U × Vᵀ ||² + λ (||U||² + ||V||²)

λ is the regularization parameter to prevent overfitting and iterations continue until the error (RMSE) stabilizes.

## Installation

- Clojure and Python
- Leiningen
- Create Python virtual environment in the root folder using command:
```bash
python3 -m venv [venv-name]
```
- Activate the virtual environment:
```bash
source [venv-name]/bin/activate
```
- Then proceed to install the requirements:
```bash
pip install -r requirements.txt
```
- To start an application, run:
```bash
lein run
```

## Literature

- [Matrix Factorization](https://www.youtube.com/watch?v=ZspR5PZemcs)
- [SentenceTransformers](https://sbert.net/docs/sentence_transformer/usage/semantic_textual_similarity.html)
- [Cheshire JSON](https://github.com/dakrone/cheshire)
- [Matrix](https://github.com/mikera/core.matrix)
- [JWT Token](https://funcool.github.io/buddy-hashers/latest/user-guide.html)
- [.edn file](https://github.com/yogthos/config)
- [Ring](https://github.com/ring-clojure/ring-json)
- [Midje](https://github.com/marick/Midje)
