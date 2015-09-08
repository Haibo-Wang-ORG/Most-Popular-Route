

(1) ########################################################
if u want to add some fields to the Data class,
u must modify:

get_size()

read_from_buffer()

write_to_buffer()

to make suer the added fields are correctly serized to disk.

(1) ########################################################
for inserting data to r-tree,
the r-tree.delete method has to be involved to write the r-tree to disk.