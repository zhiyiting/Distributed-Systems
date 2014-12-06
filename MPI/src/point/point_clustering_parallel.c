#include "mpi.h"
#include "kmeans.h"

/* MPI Code reference: https://computing.llnl.gov/tutorials/mpi/ */
int main(int argc, char **argv) {
	/* initialize the program clock */
	clock_t start_program, end_program;
    double total_time_used;
    start_program = clock();

    /* mpi variables */
	int num_tasks, rank, len, rc;
	MPI_Status Stat;
	char hostname[MPI_MAX_PROCESSOR_NAME];

	/* data type */
	MPI_Datatype point_type, cluster_type, oldtypes[2];
	int blockcounts[2];
	MPI_Aint offsets[2], extent;

	/* initialize MPI */
	rc = MPI_Init(&argc, &argv);
	if (rc != MPI_SUCCESS) {
		printf ("Error starting MPI program. Terminating.\n");
		MPI_Abort(MPI_COMM_WORLD, rc);
	}
	MPI_Comm_size(MPI_COMM_WORLD, &num_tasks);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Get_processor_name(hostname, &len);
	printf ("Number of tasks= %d My rank= %d Running on %s\n", num_tasks, rank, hostname);

	/* construct data type */
	offsets[0] = 0;
	oldtypes[0] = MPI_DOUBLE;
	blockcounts[0] = 2;

	MPI_Type_struct(1, blockcounts, offsets, oldtypes, &point_type);
	MPI_Type_commit(&point_type);

	offsets[0] = 0;
	oldtypes[0] = MPI_DOUBLE;
	blockcounts[0] = 2;
	MPI_Type_extent(MPI_DOUBLE, &extent);
	offsets[1] = 2 * extent;
	oldtypes[1] = MPI_INT;
	blockcounts[1] = 1;
	MPI_Type_struct(2, blockcounts, offsets, oldtypes, &cluster_type);
	MPI_Type_commit(&cluster_type);

	/* kmeans variables */
	point_t *dataset;
	point_t *centroids;
	int k, num_of_points;
	int startflag = 1;

	/* master process */
	if (rank == 0) {
		/* initialize variables */
		config_t *config = read_config();
    	k = config->k;
    	num_of_points = config->count;
    	dataset = init_dataset(config);
    	centroids = init_centroid(dataset, k, num_of_points);
    	cluster_sum_t *cluster_sum = (cluster_sum_t *)malloc(k * sizeof(cluster_sum_t));
    	int points_per_machine = num_of_points / (num_tasks - 1);
    	int remaining_points = num_of_points % (num_tasks - 1);
    	int points_sent = 0;
    	/* send dataset to slaves */
    	for (int i = 1; i < num_tasks; i++) {
    		int send_count = points_per_machine;
    		if (i == num_tasks - 1) {
    			send_count += remaining_points;
    		}
    		/* send number of this transmission */
    		rc = MPI_Send(&send_count, 1, MPI_INT, i, 1, MPI_COMM_WORLD);
    		/* send dataset */
    		rc = MPI_Send(&dataset[points_sent], send_count, point_type, i, 2, MPI_COMM_WORLD);
    		/* send number of clusters */
    		rc = MPI_Send(&k, 1, MPI_INT, i, 3, MPI_COMM_WORLD);
    		points_sent += send_count;
    	}
    	/* initialize new centroids */
    	point_t *new_centroids = (point_t *)malloc(k * sizeof(point_t));
    	cluster_sum_t *temp = (cluster_sum_t *)malloc(k * sizeof(cluster_sum_t));

    	/* start the kmeans clock right before kmeans iteration starts */
    	clock_t start, end;
     	double kmeans_time_used;
     	start = clock();

    	do {
    		for (int i = 0; i < k; i++) {
    			cluster_sum[i].sum_x = 0.0;
     			cluster_sum[i].sum_y = 0.0;
     			cluster_sum[i].count = 0;
     		}
     		/* send start flag and starting centroids to each process */
    		for (int i = 1; i < num_tasks; i++) {
    			rc = MPI_Send(&startflag, 1, MPI_INT, i, 0, MPI_COMM_WORLD);
    			rc = MPI_Send(centroids, k, point_type, i, 4, MPI_COMM_WORLD);
    		}
    		/* wait and receive partial cluster sum from processes */
    		for (int i = 1; i < num_tasks; i++) {
    			rc = MPI_Recv(temp, k, cluster_type, i, 5, MPI_COMM_WORLD, &Stat);
    			for (int j = 0; j < k; j++) {
    				cluster_sum[j].sum_x += temp[j].sum_x;
    				cluster_sum[j].sum_y += temp[j].sum_y;
    				cluster_sum[j].count += temp[j].count;
    			}
    		}
    		/* iterate through the cluster sum and calculate the centroids */
    		for (int i = 0; i < k; i++) {
        		point_t centroid;
        		cluster_sum_t ccluster = cluster_sum[i];
        		centroid.x = ccluster.sum_x / (double) ccluster.count;
        		centroid.y = ccluster.sum_y / (double) ccluster.count;
        		new_centroids[i] = centroid;
    		}
    	} while (!compare_replace(centroids, new_centroids, k));

    	/* track the end time after kmeans iteration ends */
    	end = clock();
     	kmeans_time_used = ((double) (end - start)) / CLOCKS_PER_SEC;
     	/* tell the slave processes the end the iteration too */
    	startflag = 0;
    	for (int i = 1; i < num_tasks; i++) {
    		rc = MPI_Send(&startflag, 1, MPI_INT, i, 0, MPI_COMM_WORLD);
    	}
    	/* clean up */
    	free(temp);
    	free(new_centroids);
    	free(cluster_sum);

    	/* print the result and running time */
    	printf("Final Result:\n");
    	print_result(centroids, k);
    	write_result(centroids, k, config);

    	end_program = clock();
     	total_time_used = ((double) (end_program - start_program)) / CLOCKS_PER_SEC;
    	printf("Kmeans time used: %lf\n", kmeans_time_used);
     	printf("Total time used: %lf\n", total_time_used);
	}
	/* slave process */
	else {
		/* receive number of points from master */
		rc = MPI_Recv(&num_of_points, 1, MPI_INT, 0, 1, MPI_COMM_WORLD, &Stat);
		dataset = (point_t *)malloc(num_of_points * sizeof(point_t));
		/* construct a local copy of part of the dataset */
		rc = MPI_Recv(dataset, num_of_points, point_type, 0, 2, MPI_COMM_WORLD, &Stat);
		/* receive cluster count from master */
		rc = MPI_Recv(&k, 1, MPI_INT, 0, 3, MPI_COMM_WORLD, &Stat);
		/* receive the start signal from master */
		rc = MPI_Recv(&startflag, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, &Stat);
		/* initialize the centroids */
		centroids = (point_t *)malloc(k * sizeof(point_t));
		while (startflag) {
			/* receive centroids */
			rc = MPI_Recv(centroids, k, point_type, 0, 4, MPI_COMM_WORLD, &Stat);
			/* compute the cluster based on centroids */
			node_t **cluster = compute_cluster(dataset, centroids, k, num_of_points);
			/* iterate and compute the [sum, count] pair for each cluster */
			cluster_sum_t *cluster_sum = compute_cluster_sum(cluster, k);
			/* send the cluster sum information back to master */
			rc = MPI_Send(cluster_sum, k, cluster_type, 0, 5, MPI_COMM_WORLD);
			/* clean up */
			free(cluster_sum);
			free(cluster);
			/* receive the signal to see if continue the iteration or not */
			rc = MPI_Recv(&startflag, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, &Stat);
		}
		free(centroids);
		free(dataset);
	}
	/* clean up */
	free(point_type);
	free(cluster_type);
	MPI_Finalize();
	return 0;
}