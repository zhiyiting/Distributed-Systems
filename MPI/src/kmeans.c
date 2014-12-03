#include "kmeans.h"

double compute_square(double v) {
    return v * v;
}

double compute_distance(point_t p1, point_t p2) {
    return compute_square(p1.x - p2.x) + compute_square(p1.y - p2.y);
}

double generate_random_double() {
    return (double)rand() / (double)RAND_MAX;
}

void write_result(point_t *centroids, int k, config_t *config) {
    FILE *fp = fopen(config->output_filename, "w");
    if (fp == NULL) {
        fprintf(stderr, "Can't open output file!\n");
        exit(1);
    }
    for (int i = 0; i < k; i++) {
        fprintf(fp, "%lf, %lf\n", centroids[i].x, centroids[i].y);
    }
    fclose(fp);
}

void print_result(point_t *centroids, int k) {
    for (int i = 0; i < k; i++) {
        printf("Cluster %d: %lf, %lf\n", i, centroids[i].x, centroids[i].y);
    }
}

config_t *read_config() {
    FILE *fp = fopen("config.txt", "r");
    if (fp == NULL) {
        fprintf(stderr, "Can't open config.txt!\n");
        exit(1);
    }
    int k;
    char input_filename[256];
    char output_filename[256];
    char temp[256];
    if (fscanf(fp, "%s %d", temp, &k) != 2) {
        fprintf(stderr, "Can't read cluster number k!\n");
        exit(1);
    }
    if (fscanf(fp, "%s %s", temp, input_filename) != 2) {
        fprintf(stderr, "Can't read input file name!\n");
        exit(1);
    }
    if (fscanf(fp, "%s %s", temp, output_filename) != 2) {
        fprintf(stderr, "Can't read output file name!\n");
        exit(1);
    }
    fclose(fp);
    config_t *config = (config_t *)malloc(sizeof(config_t));
    config->k = k;
    config->input_filename = (char *)malloc(strlen(input_filename));
    strcpy(config->input_filename, input_filename);
    config->output_filename = (char *)malloc(strlen(output_filename));
    strcpy(config->output_filename, output_filename);
    return config;
}

point_t* init_dataset(config_t *config) {
    FILE *fp = fopen(config->input_filename, "r");
    if (fp == NULL) {
        fprintf(stderr, "Can't open input file!\n");
        exit(1);
    }
    double d1, d2;
    int count = 0;
    node_t *data_node = NULL;
    while (!feof(fp)) {
        if (fscanf(fp, "%lf,%lf", &d1, &d2) != 2) break;
        node_t *p = (node_t *)malloc(sizeof(node_t));
        p->point.x = d1;
        p->point.y = d2;
        p->next = data_node;
        data_node = p;
        count++;
    }
    config->count = count;
    point_t *dataset = (point_t *)malloc(count * sizeof(point_t));
    node_t *ptr = data_node;
    for (int i = count - 1; i >= 0; i--) {
        dataset[i] = ptr->point;
        ptr = ptr->next;
    }
    free(data_node);
    return dataset;
}

/* Select k initial centroids according to kmeans++ algorithm */
/* Kmeans++ reference: http://dl.acm.org/citation.cfm?id=1283494 */
point_t *init_centroid(point_t *dataset, int k, int n) {
    point_t *centroids = (point_t *)malloc(k * sizeof(point_t));
    double *min_dist_squared = (double *)malloc(n * sizeof(double));
    int *visited = (int *)malloc(n * sizeof(int));
    time_t t;
    srand((unsigned) time(&t));
    // for simplicity, select the first point as a centroid
    centroids[0] = dataset[0];
    for (int i = 1; i < n; i++) {
        min_dist_squared[i] = compute_distance(centroids[0], dataset[i]);
        visited[i] = 0;
    }
    visited[0] = 1;
    for (int i = 1; i < k; i++) {
        double sum_dist = 0.0;
        for (int j = 0; j < n; j++) {
            if (!visited[j]) {
                sum_dist += min_dist_squared[j];
            }
        }
        double r = generate_random_double() * sum_dist;
        int next_index = -1;
        double temp_sum = 0.0;
        for (int j = 0; j < n; j++) {
            if (!visited[j]) {
                temp_sum += min_dist_squared[j];
                if (temp_sum >= r) {
                    next_index = j;
                    break;
                }
            }
        }
        if (next_index == -1) {
            for (int j = n - 1; j >= 0; j--) {
                if (!visited[j]) {
                    next_index = j;
                    break;
                }
            }
        }
        if (next_index >= 0) {
            visited[next_index] = 1;
            point_t new_centroid = dataset[next_index];
            centroids[i] = new_centroid;
            if (i < k - 1) {
                for (int j = 0; j < n; j++) {
                    if (!visited[j]) {
                        double new_dist = compute_distance(dataset[j], new_centroid);
                        if (new_dist < min_dist_squared[j]) {
                            min_dist_squared[j] = new_dist;
                        }
                    }
                }
            }
        }
        else {
        	printf("Error selecting centroids\n");
            exit(1);
        }
    }
    return centroids;
}

node_t *insert_point(node_t *ptr, point_t point) {
    node_t *node = (node_t *)malloc(sizeof(node_t));
    node->point = point;
    node->next = ptr;
    ptr = node;
    return ptr;
}

node_t **compute_cluster(point_t *dataset, point_t *centroids, int k, int n) {
    node_t **cluster = (node_t **)malloc(k * sizeof(node_t *));
    if (k <= 0) {
        printf("Invalid cluster number\n");
        exit(1);
    }
    for (int j = 0; j < n; j++) {
        point_t curpoint = dataset[j];
        printf("current point %lf, %lf\n", curpoint.x, curpoint.y);
        double min_distance = compute_distance(curpoint, centroids[0]);;
        int index = 0;
        for (int i = 1; i < k; i++) {
            double distance = compute_distance(curpoint, centroids[i]);
            if (distance < min_distance) {
                min_distance = distance;
                index = i;
            }
        }
        cluster[index] = insert_point(cluster[index], curpoint);
    }
    return cluster;
}

cluster_sum_t *compute_cluster_sum(node_t **cluster, int k) {
	printf("computing\n");
    cluster_sum_t *cluster_sum = (cluster_sum_t *)malloc(k * sizeof(cluster_sum_t));
    for (int i = 0; i < k; i++) {
        node_t *p = cluster[i];
        int count = 0;
        double sum_x = 0;
        double sum_y = 0;
        while (p != NULL) {
            sum_x += p->point.x;
            sum_y += p->point.y;
            count++;
            p = p->next;

        }
        cluster_sum[i].sum_x = sum_x;
        cluster_sum[i].sum_y = sum_y;
        cluster_sum[i].count = count;
    }
    	printf("computed\n");

    return cluster_sum;
}

point_t *compute_centroid(node_t **cluster, int k) {
	cluster_sum_t *cluster_sum = compute_cluster_sum(cluster, k);
	point_t *new_centroids = (point_t *)malloc(k * sizeof(point_t));
    for (int i = 0; i < k; i++) {
        point_t centroid;
        cluster_sum_t ccluster = cluster_sum[i];
        centroid.x = ccluster.sum_x / (double) ccluster.count;
        centroid.y = ccluster.sum_y / (double) ccluster.count;
        new_centroids[i] = centroid;
    }
    free(cluster_sum);
    return new_centroids;
}

int compare_replace(point_t *centroids, point_t *new_centroids, int k) {
    int flag = 1;
    for (int i = 0; i < k; i++) {
        if (compute_distance(centroids[i], new_centroids[i]) > THRESHOLD) {
            flag = 0;
        }
        centroids[i] = new_centroids[i];
    }
    return flag;
}

void kmeans(point_t *centroids, point_t *dataset, int k, int n) {
    node_t **cluster = compute_cluster(dataset, centroids, k, n);
    point_t *new_centroids = compute_centroid(cluster, k);
    while (!compare_replace(centroids, new_centroids, k)) {
        cluster = compute_cluster(dataset, centroids, k, n);
        new_centroids = compute_centroid(cluster, k);
    }
    free(cluster);
    free(new_centroids);
}