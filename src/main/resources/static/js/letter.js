$(function(){
    $(".close").click(delete_msg);
});

function delete_msg() {
    // TODO 删除数据
    $(this).parents(".media").remove();
}