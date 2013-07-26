# datamining-sde-runnable.jar 使用说明



## 如果直接运行
在命令行中首先先进入 .jar文件所在目录，然后输入下面形式的命令：
`java -jar datamining-sde-runnable.jar url [-o outdir] [-e encode] [-s threshold] [-i ignore] [-en enhance] [-m maxnum]`

比如：
`java -jar datamining-sde-runnable.jar http://www.baidu.com -o MDR.html -e gbk -s 0.8 -i true -en true -m 9`

或者：
`java -jar datamining-sde-runnable.jar http://www.baidu.com`

参数说明：


     * 必须参数：
     * url - 是页面的URL，比如本地`file:///C:/Users/Admin/Desktop/Mockup/SBI/test.html`或远程URL`http://www.baidu.com`
     * 
	 * 可选参数：
	 * outdir - 结果的输出路径，包括文件名，如 "C:/Users/Admin/Desktop/MDR.html";默认值："MDR.html";
	 * encode - 设置输出文件的编码格式，如 gbk,utf8 
	 * threshold - 相似度阈值，范围0-1，因为这里是归一化后的阈值;默认值：0.80;
	 * ignore - 是否忽略一些标签，如果为"true",构建tag tree时将会忽略 B, I, U 等标签，这些标签将会被当作普通文本处理;默认值：false;
	 * enhance - 是否使用加强的树匹配，如果"true"，则在计算树的归一化的相似度时用到叶子节点，否则计算树的归一化的相似度时忽略叶子节点; 默认值：false;
	 * maxmum - 搜索广义节点时，广义节点的最大个数;默认值：9;
	 *


## 如果作为依赖包使用
1. 首先在工程中导入datamining.sde-runnable.jar
2. 然后在程序中调用ExtractSinglePage类中的ExtractPage()方法，比如
<pre>
Map<String,String> map = new HashMap<String,String>();
map.put(key,value);
new ExtractSinglePage().ExtractPage(map);
</pre>

**代码示例：**

<pre>
Map<String,String> map = new HashMap<String,String>();
map.put("url", "http://www.baidu.com");
new ExtractSinglePage().ExtractPage(map);
</pre>


ExtractPage()参数说明
    
     * 参数类型为字典类型
     * 
     * 必须键值：url
     * 选填键值：-o, -s, -e, -en, -i, -m
     * 
     * 键值解释：
     * url - 是页面的URL，比如本地`file:///C:/Users/Admin/Desktop/Mockup/SBI/test.html`或远程URL`http://www.baidu.com`
     * -e - 输出文件的编码格式，如 gbk,utf8，默认使用'gbk'
	 * -o - 结果的输出路径，包括文件名，如 "C:/Users/Admin/Desktop/MDR.html";默认值："MDR.html";
	 * -s - 相似度阈值，范围0-1，因为这里是归一化后的阈值;默认值：0.80;
	 * -i - 是否忽略一些标签，如果为"true",构建tag tree时将会忽略 B, I, U 等标签，这些标签将会被当作普通文本处理;默认值：false;
	 * -en - 是否使用加强的树匹配，如果"true"，则在计算树的归一化的相似度时用到叶子节点，否则计算树的归一化的相似度时忽略叶子节点; 默认值：false;
	 * -m	- 搜索广义节点时，广义节点的最大个数;默认值：9;
	 *
    
## 补充说明：
* 如果想了解更多接口，请查看Javadoc文档，根据index.html查询。
* URL的输入需要注意，如果包含非英文字符需要进行URL编码转换。
* 由于nekohtml不支持HTML5标准的编码声明方式，所以HTML文件请使用传统HTML声明方式，比如：`<meta http-equiv="content-type" content="text/html;charset=utf8"  />`


