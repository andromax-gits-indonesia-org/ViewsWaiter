package org.cookpad.app_test.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_recipe.*
import org.cookpad.app_test.Pipelines
import org.cookpad.app_test.R
import org.cookpad.app_test.data.models.Recipe
import org.cookpad.app_test.home.recipes.RecipesFragment
import org.cookpad.views_waiter.bindOnBackground

class RecipeActivity : AppCompatActivity(), RecipePresenter.View {
    override val recipeId by lazy { intent.extras.getString(keyRecipeId) }

    val onRecipeLikedSubject = PublishSubject.create<Unit>()
    override val onRecipeLiked = onRecipeLikedSubject.hide()

    val onRecipeBookmarkedSubject = PublishSubject.create<Unit>()
    override val onRecipeBookmarked = onRecipeBookmarkedSubject.hide()

    val onRecipeUpdatedFromListSubject = PublishSubject.create<Recipe>()
    override val onRecipeUpdatedFromList = onRecipeUpdatedFromListSubject.hide()

    override val onRecipeUpdatedFromDetailSubject = PublishSubject.create<Recipe>()
    val onRecipeUpdatedFromDetail = onRecipeUpdatedFromDetailSubject.hide()

    override val onRecipeActionPipeline by lazy {
        Pipelines.recipeActionPipeline.channel(recipeId).stream().bindOnBackground(lifecycle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        lifecycle.addObserver(RecipePresenter(this))

        (fragmentRecipes as? RecipesFragment)?.apply {
            this.onRecipeUpdatedSubject = onRecipeUpdatedFromListSubject
            this.onRecipeUpdated = onRecipeUpdatedFromDetail
        }

        ivDetailLikeButton.setOnClickListener { onRecipeLikedSubject.onNext(Unit) }
        ivDetailBookmarkButton.setOnClickListener { onRecipeBookmarkedSubject.onNext(Unit) }
    }

    override fun showRecipe(recipe: Recipe) {
        recipe.apply {
            tvTitle.text = name
            tvDescription.text = description
            ivRecipeImage.setImageResource(imageRes)
        }
    }

    companion object {
        val keyRecipeId = "keyRecipeId"

        fun startRecipeActivity(context: Context, recipeId: String) {
            val intent = Intent(context, RecipeActivity::class.java).apply {
                putExtra(keyRecipeId, recipeId)
            }

            context.startActivity(intent)
        }
    }

    override fun setLiked(liked: Boolean) {
        ivDetailLikeButton.apply {
            val (drawable, colorFilter, tag) = if (liked) {
                Triple(R.drawable.ic_liked, ContextCompat.getColor(context, R.color.likedColor), "liked")
            } else {
                Triple(R.drawable.ic_like, ContextCompat.getColor(context, R.color.textColor), "")
            }

            setImageResource(drawable)
            setColorFilter(colorFilter)
            setTag(tag)
        }
    }

    override fun setBookmarked(bookmarked: Boolean) {
        ivDetailBookmarkButton.apply {
            val drawableBookmark = if (bookmarked) R.drawable.ic_bookmarked else R.drawable.ic_bookmark
            setImageResource(drawableBookmark)
            tag = if (bookmarked) "bookmarked" else ""
        }
    }
}