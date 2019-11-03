
#include "cuda_runtime.h"
#include "lodepng.c"
#include "device_launch_parameters.h"

#include <iostream>
#include <stdio.h>
#include <algorithm>

cudaError_t addWithCuda(int *c, const int *a, const int *b, unsigned int size);



__global__ void addKernel(unsigned char* image, unsigned char* image_out, int threads,
	int width, int height, int width_output,int height_output, int pix_per_thread) {

	int thread_index = blockIdx.x * 1024+ threadIdx.x;
	int starting_pixel = thread_index * pix_per_thread; // start at (including) this pixel
	int ending_pixel = thread_index == threads - 1 ? width_output * height_output : starting_pixel + pix_per_thread; //stop before this pixel

	for (int pix = starting_pixel; pix < ending_pixel; pix+=4) {
		int index_input = (width*4) + (pix % width_output)*4 + (pix/width_output*width*4); // corresponding block's top left pixel(input) *4(channel)
		int index_output = pix * 4;
		image_out[index_output] = image[index_input];
		image_out[index_output +1] = image[index_input +1];
		image_out[index_output +2] = image[index_input +2];
		image_out[index_output +3] = image[index_input +3];
	
	}


}



void loadPNG(char* input_filename, unsigned int& width, unsigned int& height, unsigned char** output_data)
{
	unsigned error;

	error = lodepng_decode32_file(output_data, &width, &height, input_filename);
	if (error) printf("error %u: %s\n", error, lodepng_error_text(error));
}

void exportPNG(char* output_filename, unsigned int width, unsigned int height,
	unsigned char* data)
{
	lodepng_encode32_file(output_filename, data, width, height);
}



void convolution(char* input_filename, char* output_filename,int dimension, int threads)
{
	unsigned char* image = nullptr;
	unsigned char* new_image=nullptr;
	unsigned int width, height;
	loadPNG(input_filename, width, height, &image);
	int size_input = width * height * 4;  // input size (channels)
	int width_output = width - dimension + 1; // output width (pixels)
	int height_output = height - dimension + 1; //output height (pixels)
	int pixels_output = width_output * height_output;
	int size_output = width_output * height_output * 4; // output size (channels)

 	unsigned char* gpu_data;
	cudaMalloc((void**)& gpu_data, size_input);
	cudaMemcpy(gpu_data, image, size_input, cudaMemcpyHostToDevice);

	unsigned char* gpu_data2;
	cudaMalloc((void**)& gpu_data2, size_output);

	int pix_per_thread = pixels_output / threads;





	int gpu_block_count = threads > 1024 ? 2 : 1;
	int threads_per_block = threads > 1024 ? (threads / 2) : threads;


	//unsigned char* image, unsigned char* image_out, in, int threads,
	//int width, int height, int width_output, int height_output, int pix_per_thread) {
	addKernel<<<gpu_block_count, threads_per_block >>> (gpu_data, gpu_data2, threads, width, height, width_output, height_output, pix_per_thread);
	cudaDeviceSynchronize();

	free(image);
	
	new_image = (unsigned char*)malloc(sizeof(unsigned char) * size_output);
	cudaMemcpy(new_image, gpu_data2, size_output, cudaMemcpyDeviceToHost);



	exportPNG(output_filename, width_output, height_output, new_image);

}


int main(int argc, char* argv[])
{
	char* input_filename = argv[1];
	int dimension = 3;
	
	convolution(input_filename, "test_output.png",dimension, 16);
    return 0;
}
