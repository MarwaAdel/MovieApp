package com.example.myapplication.details;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.ReviewListAdapter;
import com.example.myapplication.adapter.TrailerListAdapter;
import com.example.myapplication.data.MovieContract;
import com.example.myapplication.model.Movie;
import com.example.myapplication.model.Review;
import com.example.myapplication.model.Trailer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Marwa Adel on 4/3/2021.
 */
public class MovieDetailFragment extends Fragment implements FetchTrailersTask.Listener,
        TrailerListAdapter.Callbacks, FetchReviewsTask.Listener, ReviewListAdapter.Callbacks {

    @SuppressWarnings("unused")
    public static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    /**
     * The fragment argument representing the movie that this fragment
     * represents.
     */
    public static final String ARG_MOVIE = "ARG_MOVIE";
    public static final String EXTRA_TRAILERS = "EXTRA_TRAILERS";
    public static final String EXTRA_REVIEWS = "EXTRA_REVIEWS";

    private Movie mMovie;
    private TrailerListAdapter mTrailerListAdapter;
    private ReviewListAdapter mReviewListAdapter;
    private ShareActionProvider mShareActionProvider;

    @BindView(R.id.trailer_list)
    RecyclerView mRecyclerViewForTrailers;
    @BindView(R.id.review_list)
    RecyclerView mRecyclerViewForReviews;

    @BindView(R.id.movie_title)
    TextView mMovieTitleView;
    @BindView(R.id.movie_overview)
    TextView mMovieOverviewView;
    @BindView(R.id.movie_release_date)
    TextView mMovieReleaseDateView;
    @BindView(R.id.movie_user_rating)
    TextView mMovieRatingView;
    @BindView(R.id.movie_poster)
    ImageView mMoviePosterView;

    @BindView(R.id.button_watch_trailer)
    Button mButtonWatchTrailer;
    @BindView(R.id.button_mark_as_favorite)
    Button mButtonMarkAsFavorite;
    @BindView(R.id.button_remove_from_favorites)
    Button mButtonRemoveFromFavorites;

    @BindView(R.id.rating_first_star)
    ImageView rating_first_star;
    @BindView(R.id.rating_second_star)
    ImageView rating_second_star;
    @BindView(R.id.rating_third_star)
    ImageView rating_third_star;
    @BindView(R.id.rating_fourth_star)
    ImageView rating_fourth_star;
    @BindView(R.id.rating_fifth_star)
    ImageView rating_fifth_star;
//    List<ImageView> ratingStarViews;

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_MOVIE)) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
//        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout)
//                activity.findViewById(R.id.toolbar_layout);
//        if (appBarLayout != null && activity instanceof MovieDetailActivity) {
//            appBarLayout.setTitle(mMovie.getTitle());
//        }

        ImageView movieBackdrop = ((ImageView) activity.findViewById(R.id.movie_backdrop));
        if (movieBackdrop != null) {
            Picasso.with(activity)
                    .load(mMovie.getBackdropUrl(getContext()))
                    .config(Bitmap.Config.RGB_565)
                    .into(movieBackdrop);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);
        ButterKnife.bind(this, rootView);

        mMovieTitleView.setText(mMovie.getTitle());
        mMovieOverviewView.setText(mMovie.getOverview());
        mMovieReleaseDateView.setText(mMovie.getReleaseDate(getContext()));

        Picasso.with(getContext())
                .load(mMovie.getPosterUrl(getContext()))
                .config(Bitmap.Config.RGB_565)
                .into(mMoviePosterView);

        updateRatingBar();
        updateFavoriteButtons();

        // For horizontal list of trailers
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewForTrailers.setLayoutManager(layoutManager);
        mTrailerListAdapter = new TrailerListAdapter(new ArrayList<Trailer>(), this);
        mRecyclerViewForTrailers.setAdapter(mTrailerListAdapter);
        mRecyclerViewForTrailers.setNestedScrollingEnabled(false);

        // For vertical list of reviews
        mReviewListAdapter = new ReviewListAdapter(new ArrayList<Review>(), this);
        mRecyclerViewForReviews.setAdapter(mReviewListAdapter);

        // Fetch trailers only if savedInstanceState == null
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_TRAILERS)) {
            List<Trailer> trailers = savedInstanceState.getParcelableArrayList(EXTRA_TRAILERS);
            mTrailerListAdapter.add(trailers);
            mButtonWatchTrailer.setEnabled(true);
        } else {
            fetchTrailers();
        }

        // Fetch reviews only if savedInstanceState == null
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_REVIEWS)) {
            List<Review> reviews = savedInstanceState.getParcelableArrayList(EXTRA_REVIEWS);
            mReviewListAdapter.add(reviews);
        } else {
            fetchReviews();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Trailer> trailers = mTrailerListAdapter.getTrailers();
        if (trailers != null && !trailers.isEmpty()) {
            outState.putParcelableArrayList(EXTRA_TRAILERS, trailers);
        }

        ArrayList<Review> reviews = mReviewListAdapter.getReviews();
        if (reviews != null && !reviews.isEmpty()) {
            outState.putParcelableArrayList(EXTRA_REVIEWS, reviews);
        }
    }


    @Override
    public void watch(Trailer trailer, int position) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.getTrailerUrl())));
    }

    @Override
    public void read(Review review, int position) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(review.getUrl())));
    }

    @Override
    public void onFetchFinished(List<Trailer> trailers) {
        mTrailerListAdapter.add(trailers);
        mButtonWatchTrailer.setEnabled(!trailers.isEmpty());
    }

    @Override
    public void onReviewsFetchFinished(List<Review> reviews) {
        mReviewListAdapter.add(reviews);
    }

    private void fetchTrailers() {
        FetchTrailersTask task = new FetchTrailersTask(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMovie.getId());
    }

    private void fetchReviews() {
        FetchReviewsTask task = new FetchReviewsTask(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMovie.getId());
    }

    public void markAsFavorite() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (!isFavorite()) {
                    ContentValues movieValues = new ContentValues();
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                            mMovie.getId());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
                            mMovie.getTitle());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH,
                            mMovie.getPoster());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW,
                            mMovie.getOverview());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE,
                            mMovie.getUserRating());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                            mMovie.getReleaseDate());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH,
                            mMovie.getBackdrop());
                    getContext().getContentResolver().insert(
                            MovieContract.MovieEntry.CONTENT_URI,
                            movieValues
                    );
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateFavoriteButtons();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void removeFromFavorites() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (isFavorite()) {
                    getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + mMovie.getId(), null);

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateFavoriteButtons();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateRatingBar() {
        if (mMovie.getUserRating() != null && !mMovie.getUserRating().isEmpty()) {
            String userRatingStr = getResources().getString(R.string.user_rating_movie,
                    mMovie.getUserRating());
            mMovieRatingView.setText(userRatingStr);

            float userRating = Float.valueOf(mMovie.getUserRating()) / 2;
            int integerPart = (int) userRating;

            // Fill stars
//            for (int i = 0; i < integerPart; i++) {
//                ratingStarViews.get(i).setImageResource(R.drawable.ic_star_black_24dp);
//            }
            rating_first_star.setImageResource(R.drawable.ic_star_black_24dp);
            rating_second_star.setImageResource(R.drawable.ic_star_black_24dp);
            rating_third_star.setImageResource(R.drawable.ic_star_black_24dp);
            rating_fourth_star.setImageResource(R.drawable.ic_star_black_24dp);
            rating_first_star.setImageResource(R.drawable.ic_star_black_24dp);
            // Fill half star
            if (Math.round(userRating) > integerPart) {
//                ratingStarViews.get(integerPart).setImageResource(
//                        R.drawable.ic_star_half_black_24dp);
                rating_first_star.setImageResource(R.drawable.ic_star_half_black_24dp);
                rating_second_star.setImageResource(R.drawable.ic_star_half_black_24dp);
                rating_third_star.setImageResource(R.drawable.ic_star_half_black_24dp);
                rating_fourth_star.setImageResource(R.drawable.ic_star_half_black_24dp);
                rating_first_star.setImageResource(R.drawable.ic_star_half_black_24dp);
            }

        } else {
            mMovieRatingView.setVisibility(View.GONE);
        }
    }

    private void updateFavoriteButtons() {
        // Needed to avoid "skip frames".
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                return isFavorite();
            }

            @Override
            protected void onPostExecute(Boolean isFavorite) {
                if (isFavorite) {
                    mButtonRemoveFromFavorites.setVisibility(View.VISIBLE);
                    mButtonMarkAsFavorite.setVisibility(View.GONE);
                } else {
                    mButtonMarkAsFavorite.setVisibility(View.VISIBLE);
                    mButtonRemoveFromFavorites.setVisibility(View.GONE);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mButtonMarkAsFavorite.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        markAsFavorite();
                    }
                });

        mButtonWatchTrailer.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTrailerListAdapter.getItemCount() > 0) {
                            watch(mTrailerListAdapter.getTrailers().get(0), 0);
                        }
                    }
                });

        mButtonRemoveFromFavorites.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeFromFavorites();
                    }
                });
    }

    private boolean isFavorite() {
        Cursor movieCursor = getContext().getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + mMovie.getId(),
                null,
                null);

        if (movieCursor != null && movieCursor.moveToFirst()) {
            movieCursor.close();
            return true;
        } else {
            return false;
        }
    }
}
