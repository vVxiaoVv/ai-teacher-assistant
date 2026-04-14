from PIL import Image
import numpy as np

# 打开图片
img_path = '/Users/tal/Documents/code/project/ai-teacher-assistant/admin-platform/public/img/school-building.jpg'
img = Image.open(img_path)

# 缩小图片以提高处理速度
img = img.resize((100, 100))

# 转换为numpy数组并重塑为二维数组
img_array = np.array(img).reshape(-1, 3)

# 获取唯一颜色及其出现次数
unique_colors, counts = np.unique(img_array, axis=0, return_counts=True)

# 找到出现次数最多的颜色
main_color = unique_colors[counts.argmax()]

# 转换为十六进制颜色代码
hex_color = '#{:02x}{:02x}{:02x}'.format(main_color[0], main_color[1], main_color[2])

print(f'背景图的主体颜色十六进制代码: {hex_color}')
print(f'RGB值: {main_color}')
