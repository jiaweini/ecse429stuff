
#include "cuda_runtime.h"
#include "lodepng.c"
#include "device_launch_parameters.h"

#include <stdio.h>

void convolution(char* input_filename,int dimension, int threads);

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

__global__ void addKernel()
{
  
}

void convolution(char* input_filename, int dimension, int threads) {
	unsigned char* image = nullptr;
	unsigned int width, height;
	loadPNG(input_filename, width, height, &image);
	int size = width * height * 4;


	int mul_op_width = width - dimension + 1;
	int mul_op_height = height - dimension + 1;
	int total_mul_op = mul_op_width*mul_op_height; // Number of Matrix Multiplication to be performed;




	return;
}

int main()
{
    
	char* input_filename = "input.png";
	int dimension = 3; //3,5 or 7
	int threads = 1;

	convolution(input_filename,dimension,threads);

   
    return 0;
}

