import com.github.houbb.markdown.toc.core.impl.AtxMarkdownToc;

/**
 * @Author:jiangqikun
 * @Date:2021/4/25 9:32
 **/

public class MyFileAdd {


    public static void main(String[] args) {
        AtxMarkdownToc.newInstance().genTocFile("JVM.md");
    }


}
