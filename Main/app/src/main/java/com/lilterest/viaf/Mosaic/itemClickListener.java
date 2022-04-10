package com.lilterest.viaf.Mosaic;

/**
 * Author CodeBoy722
 */
public interface itemClickListener {

    /**
     * Called when a picture is clicked
     * @param holder The ViewHolder for the clicked picture
     * @param position The position in the grid of the picture that was clicked
     */
    void onPicClicked(String pictureFolderPath);
}
