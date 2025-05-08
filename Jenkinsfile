@Library('socrata-pipeline-library@sarahs/EN-77895/checkout-before-checking-release-tags') _

commonPipeline(
  jobName: 'region-coder',
  language: 'scala',
  projects: [
    [
      name: 'region-coder',
      compiled: true,
      deploymentEcosystem: 'marathon-mesos',
      paths: [
        dockerBuildContext: 'docker',
      ],
      type: 'service'
    ]
  ],
  teamsChannelWebhookId: 'WORKFLOW_IQ',
)
