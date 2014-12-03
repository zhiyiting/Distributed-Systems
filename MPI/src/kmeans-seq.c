#include "kmeans.h"

int main() {
    config_t *config = read_config();
    int k = config->k;
    point_t *dataset = init_dataset(config);
    int num_of_points = config->count;
    point_t *centroids = init_centroid(dataset, k, num_of_points);
    kmeans(centroids, dataset, k, num_of_points);
    print_result(centroids, k);
    write_result(centroids, k, config);
    return 0;
}