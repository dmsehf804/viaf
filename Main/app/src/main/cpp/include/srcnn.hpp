/* Program 	: Image Super-Resolution using deep Convolutional Neural Networks
 * Author  	: Wang Shu
 * Date		: Sun 13 Sep, 2015
 * Descrip.	:
* */

#ifndef _SRCNN_HPP
#define _SRCNN_HPP

#include <iostream>
#include <iomanip>

#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "android/log.h"
#include "convolution1.h"
#include "convolution2.h"
#include "convolution3.h"

using namespace std;
using namespace cv;

/* Marco Definition */
#define IMAGE_WIDTH		960		// the image width
#define IMAGE_HEIGHT		540		// the image height
#define CONV1_FILTERS		64		// the first convolutional layer
#define CONV2_FILTERS		32		// the second convolutional layer
//#define DEBUG			1		// show the debug info
//#define DISPLAY			1		// display the temp image
//#define SAVE			0		// save the temp image
//#define CUBIC			1

/* Load the convolutional data */

/* Function Declaration */

/***
 * FuncName	: main
 * Function	: the entry of the program
 * Parameter	: pImgOrigin -- the input image.
 * Output	: pImgBGROut -- the output image.
***/
Mat srcnn(Mat pImgOriginMat, int UP_SCALE)
{
	IplImage* pImgOrigin = new IplImage (pImgOriginMat);
	IplImage* pImgYCbCr = cvCreateImage(CvSize(pImgOrigin->width, pImgOrigin->height), IPL_DEPTH_8U, 3);
	cvCvtColor(pImgOrigin, pImgYCbCr, CV_BGR2YCrCb);

	IplImage* pImgY = cvCreateImage(CvSize(pImgYCbCr->width, pImgYCbCr->height), IPL_DEPTH_8U, 1);
	IplImage* pImgCb = cvCreateImage(CvSize(pImgYCbCr->width, pImgYCbCr->height), IPL_DEPTH_8U, 1);
	IplImage* pImgCr = cvCreateImage(CvSize(pImgYCbCr->width, pImgYCbCr->height), IPL_DEPTH_8U, 1);
	cvSplit(pImgYCbCr, pImgY, pImgCb, pImgCr, 0);

	IplImage* pImg = cvCreateImage(CvSize(UP_SCALE * pImgY->width, UP_SCALE * pImgY->height), IPL_DEPTH_8U, 1);
	cvResize(pImgY, pImg, CV_INTER_CUBIC);

	IplImage* pImgConv1[CONV1_FILTERS];
	for (int i = 0; i < CONV1_FILTERS; i++)
	{
		pImgConv1[i] = cvCreateImage(CvSize(pImg->width, pImg->height), IPL_DEPTH_16U, 1);
		Convolution99(pImg, pImgConv1[i], weights_conv1_data[i], biases_conv1[i]);
	}
	cvSaveImage("Pictures/Conv1.bmp", pImgConv1[8]);

	/*第二卷积层*/
	IplImage* pImgConv2[CONV2_FILTERS];
	for (int i = 0; i < CONV2_FILTERS; i++)
	{
		pImgConv2[i] = cvCreateImage(CvSize(pImg->width, pImg->height), IPL_DEPTH_16U, 1);
		Convolution11(pImgConv1, pImgConv2[i], weights_conv2_data[i], biases_conv2[i]);
		cout << "Convolution Layer II : " << setw(2) << i+1 << "/32 Cell Complete..." << endl;
	}
	//cvNamedWindow("Conv2");
	//cvShowImage("Conv2", pImgConv2[31]);
	//ShowImgData(pImgConv2[31]);
	cvSaveImage("Pictures/Conv2.bmp", pImgConv2[31]);

	/*第三卷积层*/
	IplImage* pImgConv3 = cvCreateImage(CvSize(pImg->width, pImg->height), IPL_DEPTH_8U, 1);
	Convolution55(pImgConv2, pImgConv3, weights_conv3_data, biases_conv3);
	//cvNamedWindow("Conv3");
	//cvShowImage("Conv3", pImgConv3);
	//ShowImgData(pImgConv3);
	cvSaveImage("Pictures/Conv3.bmp", pImgConv3);
	cout << "Convolution Layer III : 100% Complete..." << endl;

	/*合成输出*/
	IplImage* pImgCb2 = cvCreateImage(CvSize(UP_SCALE * pImgCb->width, UP_SCALE * pImgCb->height), IPL_DEPTH_8U, 1);
	cvResize(pImgCb, pImgCb2, CV_INTER_CUBIC);
	IplImage* pImgCr2 = cvCreateImage(CvSize(UP_SCALE * pImgCr->width, UP_SCALE * pImgCr->height), IPL_DEPTH_8U, 1);
	cvResize(pImgCr, pImgCr2, CV_INTER_CUBIC);
	IplImage* pImgYCbCrOut = cvCreateImage(CvSize(pImg->width, pImg->height), IPL_DEPTH_8U, 3);
	cvMerge(pImgConv3, pImgCb2, pImgCr2, 0, pImgYCbCrOut);
	IplImage* pImgBGROut = cvCreateImage(CvSize(pImg->width, pImg->height), IPL_DEPTH_8U, 3);
	cvCvtColor(pImgYCbCrOut, pImgBGROut, CV_YCrCb2BGR);

	cvMerge(pImg, pImgCb2, pImgCr2, 0, pImgYCbCrOut);
	cvCvtColor(pImgYCbCrOut, pImgBGROut, CV_YCrCb2BGR);

	Mat converted_mat = cvarrToMat(pImgBGROut );

	return converted_mat;
}

/***
 * FuncName	: Convolution99
 * Function	: Complete one cell in the first Convolutional Layer
 * Parameter	: src - the original input image
 *		  dst - the output image
 *		  kernel - the convolutional kernel
 *		  bias - the cell bias
 * Output	: <void>
***/
void Convolution99(Mat& src, Mat& dst, float kernel[9][9], float bias)
{
	/* Expand the src image */
	Mat src2;
	src2.create(Size(src.cols + 8, src.rows + 8), CV_8U);
	
	for (int row = 0; row < src2.rows; row++)
	{
		for (int col = 0; col < src2.cols; col++)
		{
			int tmpRow = row - 4;
			int tmpCol = col - 4;

			if (tmpRow < 0)
				tmpRow = 0;
			else if (tmpRow >= src.rows)
				tmpRow = src.rows - 1;

			if (tmpCol < 0)
				tmpCol = 0;
			else if (tmpCol >= src.cols)
				tmpCol = src.cols - 1;

			src2.at<unsigned char>(row, col) = src.at<unsigned char>(tmpRow, tmpCol);
		}
	}
#ifdef DISPLAY
	//imshow("Src2", src2);
#endif

	/* Complete the Convolution Step */
	for (int row = 0; row < dst.rows; row++)
	{
		for (int col = 0; col < dst.cols; col++)
		{
			/* Convolution */
			float temp = 0;
			for (int i = 0; i < 9; i++)
			{
				for (int j = 0; j < 9; j++)
				{
					temp += kernel[i][j] * src2.at<unsigned char>(row + i, col + j);
				}
			}
			temp += bias;

			/* Threshold */
			temp = (temp >= 0) ? temp : 0;

			dst.at<float>(row, col) = temp;
		}
	}

	return;
}

/***
 * FuncName	: Convolution11
 * Function	: Complete one cell in the second Convolutional Layer
 * Parameter	: src - the first layer data
 *		  dst - the output data
 *		  kernel - the convolutional kernel
 *		  bias - the cell bias
 * Output	: <void>
***/
void Convolution11(vector<Mat>& src, Mat& dst, float kernel[CONV1_FILTERS], float bias)
{
	for (int row = 0; row < dst.rows; row++)
	{
		for (int col = 0; col < dst.cols; col++)
		{
			/* Process with each pixel */
			float temp = 0;
			for (int i = 0; i < CONV1_FILTERS; i++)
			{
				temp += src[i].at<float>(row, col) * kernel[i];
			}
			temp += bias;

			/* Threshold */
			temp = (temp >= 0) ? temp : 0;

			dst.at<float>(row, col) = temp;
		}
	}

	return;
}

/***
 * FuncName	: Convolution55
 * Function	: Complete the cell in the third Convolutional Layer
 * Parameter	: src - the second layer data 
 *		  dst - the output image
 *		  kernel - the convolutional kernel
 *		  bias - the cell bias
 * Output	: <void>
***/
void Convolution55(vector<Mat>& src, Mat& dst, float kernel[32][5][5], float bias)
{
	/* Expand the src image */
	vector<Mat> src2(CONV2_FILTERS);
	for (int i = 0; i < CONV2_FILTERS; i++)
	{
		src2[i].create(Size(src[i].cols + 4, src[i].rows + 4), CV_32F);
		for (int row = 0; row < src2[i].rows; row++)
		{
			for (int col = 0; col < src2[i].cols; col++)
			{
				int tmpRow = row - 2;
				int tmpCol = col - 2;

				if (tmpRow < 0)
					tmpRow = 0;
				else if (tmpRow >= src[i].rows)
					tmpRow = src[i].rows - 1;

				if (tmpCol < 0)
					tmpCol = 0;
				else if (tmpCol >= src[i].cols)
					tmpCol = src[i].cols - 1;

				src2[i].at<float>(row, col) = src[i].at<float>(tmpRow, tmpCol);
			}
		}
	}

	/* Complete the Convolution Step */
	for (int row = 0; row < dst.rows; row++)
	{
		for (int col = 0; col < dst.cols; col++)
		{
			float temp = 0;

			for (int i = 0; i < CONV2_FILTERS; i++)
			{
				double temppixel = 0;
				for (int m = 0; m < 5; m++)
				{
					for (int n = 0; n < 5; n++)
					{
						temppixel += kernel[i][m][n] * src2[i].at<float>(row + m, col + n);
					}
				}

				temp += temppixel;
			}

			temp += bias;

			/* Threshold */
			temp = (temp >= 0) ? temp : 0;
			temp = (temp <= 255) ? temp : 255;

			dst.at<unsigned char>(row, col) = (unsigned char)temp;
		}
#ifdef DEBUG
		cout << "Convolutional Layer III : " << setw(4) << row + 1 << '/' << dst.rows << " Complete ..." << endl;
#endif
	}

	return;
}

#endif // SRCNN_HPP