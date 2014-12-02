#ifndef _KMEANS_H_
#define _KMEANS_H_

typedef struct {
	int k, count;
	char *input_filename;
	char *output_filename;
} config_t;

typedef struct {
	double x, y;
} point_t;

typedef struct node_t {
	point_t point;
	struct node_t *next;
} node_t;

#endif