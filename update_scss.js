const fs = require('fs');
const path = '/Users/bickpenna/GitHub/BugBoard26/bugboard-frontend/src/app/features/report.component/report.component.scss';

let content = fs.readFileSync(path, 'utf8');

// 1. Remove Design Tokens completely
content = content.replace(/\/\/ -- Design Tokens[\s\S]*?(?=\/\/ -- Animations --)/, '');

// 2. Remove dead CSS (export, sparkline, activity chart)
content = content.replace(/\/\/ -- Export Button --[\s\S]*?(?=\/\/ -- KPI Cards --)/, '');
content = content.replace(/\/\/ -- Sparkline Mini-Bars --[\s\S]*?(?=\/\/ -- Insight Card --)/, '');

// 3. Replace variables with Bootstrap variables
const replacements = [
  [/\$color-muted/g, 'var(--bs-secondary)'],
  [/\$color-primary-light/g, 'var(--bs-primary-bg-subtle)'],
  [/\$color-primary-container/g, 'var(--bs-primary)'],
  [/\$color-danger/g, 'var(--bs-danger)'],
  [/\$color-warning/g, 'var(--bs-warning)'],
  [/\$color-success/g, 'var(--bs-success)'],
  [/\$color-surface/g, 'var(--bs-light)'],
  [/\$color-card-bg/g, 'var(--bs-body-bg)'],
  [/\$color-primary/g, 'var(--bs-primary)'],
  [/\$color-border-subtle/g, 'var(--bs-border-color)'],
  
  // Specific rgba updates for Bootstrap RGB variables
  [/rgba\(var\(--bs-primary\), 0\.1\)/g, 'rgba(var(--bs-primary-rgb), 0.1)'],
  [/rgba\(var\(--bs-primary\), 0\.03\)/g, 'rgba(var(--bs-primary-rgb), 0.03)'],
  [/rgba\(var\(--bs-primary\), 0\.12\)/g, 'rgba(var(--bs-primary-rgb), 0.12)'],
  [/rgba\(var\(--bs-danger\), 0\.1\)/g, 'rgba(var(--bs-danger-rgb), 0.1)'],
  [/rgba\(var\(--bs-warning\), 0\.1\)/g, 'rgba(var(--bs-warning-rgb), 0.1)'],
  [/rgba\(var\(--bs-light\), 0\.5\)/g, 'rgba(var(--bs-light-rgb), 0.5)'],
  [/rgba\(var\(--bs-border-color\), 0\.5\)/g, 'var(--bs-border-color-translucent)'],
];

for (const [regex, replacement] of replacements) {
  content = content.replace(regex, replacement);
}

fs.writeFileSync(path, content, 'utf8');
console.log('SCSS updated successfully!');
let scssContent = fs.readFileSync(path, 'utf8');

// Replace old .kpi-progress
scssContent = scssContent.replace(/\.kpi-progress \{[\s\S]*?\}/, `progress.kpi-progress {
  appearance: none;
  -webkit-appearance: none;
  height: 4px;
  border-radius: 999px;
  background-color: rgba(0, 0, 0, 0.06);
  border: none;
  overflow: hidden;

  &::-webkit-progress-bar {
    background-color: transparent;
  }
  
  &::-webkit-progress-value {
    transition: width 0.6s cubic-bezier(0.16, 1, 0.3, 1);
  }
  
  &::-moz-progress-bar {
    transition: width 0.6s cubic-bezier(0.16, 1, 0.3, 1);
  }

  &.kpi-progress-danger::-webkit-progress-value { background-color: var(--bs-danger); }
  &.kpi-progress-danger::-moz-progress-bar { background-color: var(--bs-danger); }

  &.kpi-progress-primary::-webkit-progress-value { background-color: var(--bs-primary); }
  &.kpi-progress-primary::-moz-progress-bar { background-color: var(--bs-primary); }

  &.kpi-progress-warning::-webkit-progress-value { background-color: var(--bs-warning); }
  &.kpi-progress-warning::-moz-progress-bar { background-color: var(--bs-warning); }
}`);

fs.writeFileSync(path, scssContent, 'utf8');
console.log('Progress SCSS applied!');
