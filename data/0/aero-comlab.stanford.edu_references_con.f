program double c c c reads in unformatted plot3d style mesh in double precision c converts it to single precision and writes out the result c c implicit none integer nblocks integer allocatable il jl kl integer i j k l ii jj kk integer iii jjj kkk n integer iread iwrit real 8 allocatable x real 4 allocatable x1 c c c iread 18 iwrit 19 open unit iread file mesh double prec form unformatted open unit iwrit file mesh sngl prec form unformatted c c number of blocks c read iread nblocks write iwrit nblocks write nblocks nblocks c c grid dimensions for each block c allocate il nblocks allocate jl nblocks allocate kl nblocks read iread il i jl i kl i i 1 nblocks write iwrit il i jl i kl i i 1 nblocks c c handle mesh for each block c do n 1 nblocks ii il n jj jl n kk kl n allocate x ii jj kk 3 read iread x i j k l i 1 ii j 1 jj k 1 kk l 1 3 allocate x1 ii jj kk 3 x1 sngl x convert from double precision to single precision write iwrit x1 i j k l i 1 ii j 1 jj k 1 kk l 1 3 deallocate x deallocate x1 write block n ii jj kk end do c stop end