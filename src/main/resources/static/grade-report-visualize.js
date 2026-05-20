// 成绩报表可视化 - 粘贴到 Apifox 的 "后置操作 → 自定义脚本" 中
// 适用接口：GET /teacher/grades（JSON 列表）、GET /grades/report（Excel 下载）

var contentType = pm.response.headers.get('Content-Type') || '';
var isJson = contentType.indexOf('application/json') !== -1;
var isExcel = contentType.indexOf('spreadsheet') !== -1 || contentType.indexOf('officedocument') !== -1;

// ==================== Excel 下载模式 ====================
if (isExcel) {
    var disposition = pm.response.headers.get('Content-Disposition') || '';
    var fileName = '成绩报表.xlsx';
    var match = disposition.match(/filename\*?=(?:UTF-8''|"?) ?([^";]+)/);
    if (match) { fileName = decodeURIComponent(match[1]); }
    var size = pm.response.size || pm.response.stream ? pm.response.stream.length : 0;
    var sizeKB = (size / 1024).toFixed(1);

    var excelTemplate = `
<!DOCTYPE html>
<html lang="zh-CN">
<head><meta charset="utf-8">
<style>
* { margin:0; padding:0; box-sizing:border-box; }
body { font-family:'PingFang SC','Microsoft YaHei',sans-serif; background:#f0f2f5; display:flex; align-items:center; justify-content:center; min-height:100vh; }
.card { background:#fff; border-radius:16px; padding:48px; text-align:center; box-shadow:0 2px 8px rgba(0,0,0,.08); max-width:420px; }
.icon { font-size:56px; margin-bottom:20px; }
.title { font-size:20px; font-weight:700; color:#1a1a2e; margin-bottom:8px; }
.desc { color:#666; font-size:14px; margin-bottom:24px; line-height:1.8; }
.badge { display:inline-flex; align-items:center; gap:6px; background:#f6ffed; border:1px solid #b7eb8f; border-radius:8px; padding:8px 16px; font-size:13px; color:#389e0d; margin-bottom:16px; }
.meta { display:flex; gap:24px; justify-content:center; margin-top:8px; }
.meta-item { text-align:center; }
.meta-val { font-size:22px; font-weight:600; color:#1a1a2e; }
.meta-label { font-size:12px; color:#999; margin-top:2px; }
.status-ok { color:#52c41a; }
</style></head>
<body>
<div class="card">
  <div class="icon">📥</div>
  <div class="title">成绩报表下载成功</div>
  <div class="desc">
    浏览器已开始下载 Excel 文件<br>
    请查看浏览器的下载列表
  </div>
  <div class="badge">
    <span>✅</span> 导出成功
  </div>
  <div class="meta">
    <div class="meta-item">
      <div class="meta-val">{{fileName}}</div>
      <div class="meta-label">文件名</div>
    </div>
    <div class="meta-item">
      <div class="meta-val">{{sizeKB}} KB</div>
      <div class="meta-label">文件大小</div>
    </div>
    <div class="meta-item">
      <div class="meta-val status-ok">200</div>
      <div class="meta-label">状态码</div>
    </div>
  </div>
</div>
</body>
</html>`;

    pm.visualizer.set(excelTemplate, {
        fileName: fileName,
        sizeKB: sizeKB
    });

} else {

// ==================== JSON 数据可视化模式 ====================
try {
    var rawText = pm.response.text();
    var resp = JSON.parse(rawText);
} catch (e) {
    var errTemplate = `
<!DOCTYPE html>
<html lang="zh-CN">
<head><meta charset="utf-8">
<style>
body { font-family:'PingFang SC',sans-serif; background:#f0f2f5; display:flex; align-items:center; justify-content:center; min-height:100vh; margin:0; }
.card { background:#fff; border-radius:16px; padding:48px 64px; text-align:center; box-shadow:0 2px 8px rgba(0,0,0,.08); }
.icon { font-size:48px; margin-bottom:16px; }
.title { font-size:18px; font-weight:600; color:#ff4d4f; margin-bottom:8px; }
.desc { font-size:13px; color:#999; }
</style></head>
<body>
<div class="card">
  <div class="icon">⚠️</div>
  <div class="title">响应格式无法解析</div>
  <div class="desc">{{error}}</div>
</div>
</body>
</html>`;
    pm.visualizer.set(errTemplate, { error: String(e) });
    throw e;
}

var list = (resp.data && resp.data.list) ? resp.data.list : [];

var totalScore = 0, maxScore = -Infinity, minScore = Infinity;
var passCount = 0, failCount = 0;

list.forEach(function (item) {
    var s = item.score != null ? Number(item.score) : 0;
    totalScore += s;
    if (s > maxScore) maxScore = s;
    if (s < minScore) minScore = s;
    if (s >= 60) passCount++; else failCount++;
});

var avgScore = list.length > 0 ? (totalScore / list.length).toFixed(1) : 0;
var passRate = list.length > 0 ? ((passCount / list.length) * 100).toFixed(1) : 0;

var template = `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<style>
* { margin:0; padding:0; box-sizing:border-box; }
body { font-family:'PingFang SC','Microsoft YaHei',sans-serif; background:#f0f2f5; padding:24px; }
.card { background:#fff; border-radius:12px; padding:24px; margin-bottom:20px; box-shadow:0 1px 3px rgba(0,0,0,.08); }
.card-title { font-size:18px; font-weight:600; color:#1a1a2e; margin-bottom:16px; display:flex; align-items:center; gap:8px; }
.stat-row { display:flex; gap:16px; flex-wrap:wrap; margin-bottom:8px; }
.stat-item { flex:1; min-width:100px; background:#f8f9fc; border-radius:8px; padding:16px; text-align:center; }
.stat-value { font-size:26px; font-weight:700; color:#1a1a2e; }
.stat-label { font-size:12px; color:#8c8c8c; margin-top:4px; }
.stat-green .stat-value { color:#52c41a; }
.stat-red .stat-value { color:#ff4d4f; }
.stat-blue .stat-value { color:#1677ff; }
table { width:100%; border-collapse:collapse; font-size:14px; }
thead th { background:#f0f2f5; color:#1a1a2e; font-weight:600; padding:12px 16px; text-align:left; border-bottom:2px solid #e8e8e8; }
tbody td { padding:12px 16px; border-bottom:1px solid #f0f0f0; color:#333; }
tbody tr:hover { background:#fafafa; }
.pass { color:#52c41a; font-weight:600; }
.fail { color:#ff4d4f; font-weight:600; }
.excellent { color:#722ed1; font-weight:600; }
.empty { text-align:center; color:#999; padding:40px 0; }
</style>
</head>
<body>

<div class="card">
  <div class="card-title">📊 成绩概览</div>
  <div class="stat-row">
    <div class="stat-item stat-blue">
      <div class="stat-value">{{avgScore}}</div>
      <div class="stat-label">平均分</div>
    </div>
    <div class="stat-item">
      <div class="stat-value">{{maxScore}}</div>
      <div class="stat-label">最高分</div>
    </div>
    <div class="stat-item">
      <div class="stat-value">{{minScore}}</div>
      <div class="stat-label">最低分</div>
    </div>
    <div class="stat-item stat-green">
      <div class="stat-value">{{passRate}}%</div>
      <div class="stat-label">及格率</div>
    </div>
    <div class="stat-item">
      <div class="stat-value">{{totalCount}}</div>
      <div class="stat-label">总人数</div>
    </div>
  </div>
</div>

<div class="card">
  <div class="card-title">📋 成绩明细</div>
  {{#if list.length}}
  <table>
    <thead>
      <tr>
        <th>序号</th>
        <th>成绩ID</th>
        <th>分数</th>
        <th>等级</th>
        <th>评语</th>
        <th>录入时间</th>
      </tr>
    </thead>
    <tbody>
      {{#each list}}
      <tr>
        <td>{{inc @index}}</td>
        <td>{{gradeId}}</td>
        <td><span class="{{scoreClass}}">{{score}}</span></td>
        <td>{{gradeLevel}}</td>
        <td>{{comment}}</td>
        <td>{{createdAt}}</td>
      </tr>
      {{/each}}
    </tbody>
  </table>
  {{else}}
  <div class="empty">暂无成绩数据</div>
  {{/if}}
</div>

</body>
</html>
`;

var enrichedList = list.map(function (item) {
    var score = item.score != null ? Number(item.score) : 0;
    var cls = '';
    if (score >= 90) cls = 'excellent';
    else if (score >= 60) cls = 'pass';
    else cls = 'fail';
    return Object.assign({}, item, {
        scoreDisplay: score,
        scoreClass: cls
    });
});

Handlebars.registerHelper('inc', function (index) {
    return index + 1;
});

pm.visualizer.set(template, {
    avgScore: avgScore,
    maxScore: maxScore === -Infinity ? '-' : maxScore,
    minScore: minScore === Infinity ? '-' : minScore,
    passRate: passRate,
    totalCount: list.length,
    list: enrichedList
});

}
